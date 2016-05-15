package com.projects.tradingMachine.services.database.noSql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.projects.tradingMachine.services.database.DataManager;
import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.marketData.MarketData;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderTimeInForce;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class MongoDBManager implements DataManager {
	private static Logger logger = LoggerFactory.getLogger(MongoDBManager.class);
	
	private final MongoDBConnection mongoDBConnection;
	private final MongoCollection<Document> filledOrdersCollection;
	private final MongoCollection<Document> marketDataCollection;
	
	public MongoDBManager(final MongoDBConnection mongoDBConnection, final String filledOrdersCollectionName, final String marketDataCollectionName) {
		this.mongoDBConnection = mongoDBConnection;
		filledOrdersCollection = mongoDBConnection.getMongoDatabase().getCollection(filledOrdersCollectionName);
		marketDataCollection = marketDataCollectionName == null ? null : mongoDBConnection.getMongoDatabase().getCollection(marketDataCollectionName);
	}
	
	public MongoDBManager(final MongoDBConnection mongoDBConnection, final String filledOrdersCollectionName) {
		this(mongoDBConnection, filledOrdersCollectionName, null);
	}

	@Override
	public void storeOrder(final SimpleOrder order) {
		filledOrdersCollection.replaceOne(new Document("FilledOrder", order.getID()), ConvertSimpleOrderToBSONDocument(order), 
				new UpdateOptions().upsert(true));
		logger.debug(order+" added to collection: "+filledOrdersCollection.toString());
	}
	
	@Override
	public List<SimpleOrder> getOrders(final Optional<OrderType> orderType) {
		long startTime = System.currentTimeMillis();
		final List<SimpleOrder> result = new ArrayList<SimpleOrder>();
		final MongoCursor<Document> cursor = orderType.isPresent() ? filledOrdersCollection.find(new Document("Type", orderType.get().toString())).iterator() : filledOrdersCollection.find().iterator();
		try {
		    while (cursor.hasNext()) {
		    	final Document doc = cursor.next();
		    	result.add(new SimpleOrder(doc.getString("ID"), doc.getString("Symbol"), doc.getInteger("Quantity"), 
		    		 OrderSide.fromString(doc.getString("Side")), OrderType.fromString(doc.getString("Type")), OrderTimeInForce.fromString(doc.getString("TimeInForce")), 
		    		 doc.getDouble("LimitPrice"), doc.getDouble("StopPrice"), doc.getDouble("Price"), doc.getString("OriginalID"), doc.getDate("FillDate")));
		    }
		} finally {
		    cursor.close();
		}
		logger.info("Time taken to retrieve orders: "+(startTime - System.currentTimeMillis())+" ms.");
		return result;
	}
	
	@Override
	public void addMarketDataItems(final List<MarketData> marketDataItems, final boolean deleteFirst) {
		logger.debug("Starting to store "+ marketDataItems.size()+" MarketData items...");
		if (deleteFirst) 
			marketDataCollection.deleteMany(new Document());
		final List<Document> docs = new ArrayList<Document>();
		marketDataItems.forEach(marketDataItem -> docs.add(ConvertMarketDataToBSONDocument(marketDataItem)));
		marketDataCollection.insertMany(docs);
		logger.debug("Data stored successfully");
	}

	@Override
	public void close() throws Exception {
		mongoDBConnection.close();
	}

	private static Document ConvertSimpleOrderToBSONDocument(final SimpleOrder order) {
		return new Document("ID", order.getID())
		        .append("Symbol", order.getSymbol())
		        .append("Quantity",order.getQuantity())
		        .append("Side", order.getSide().toString())
		        .append("Type", order.getType().toString())	
		        .append("TimeInForce", order.getTimeInForce().toString())
		        .append("LimitPrice", order.getLimit())
		        .append("StopPrice", order.getStop())
		        .append("Price", order.getAvgPx())
		        .append("OriginalID", order.getOriginalID())
				.append("FillDate", new Date());
	}
	
	private static Document ConvertMarketDataToBSONDocument(final MarketData marketData) {
		return new Document("Symbol", marketData.getSymbol())
		        .append("Ask", marketData.getAsk())
		        .append("Bid",marketData.getBid())
		        .append("AskSize", marketData.getAskSize())
		        .append("BidSize", marketData.getBidSize())	
		        .append("QuoteTime", marketData.getQuoteTime());
	}
	
	public static void main(final String[] args) throws NumberFormatException, Exception {
		final Properties p = Utility.getApplicationProperties("tradingMachineServices.properties"); 
		try(final DataManager mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.filledOrdersCollection"))) {
			//System.out.println(mongoDBManager.getOrders(Optional.of(OrderType.STOP)).stream().mapToDouble(SimpleOrder::getAvgPx).summaryStatistics());
			//mongoDBManager.getOrders(Optional.of(OrderType.LIMIT)).stream().map(SimpleOrder::getAvgPx).forEach(System.out::println);
			//System.out.println(mongoDBManager.getOrders(Optional.ofNullable(null)).stream().collect(Collectors.groupingBy(SimpleOrder::getType, Collectors.counting())));
		}
	}
}