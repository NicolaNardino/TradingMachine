package com.projects.tradingMachine.services.database.noSql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.projects.tradingMachine.services.database.DataManager;
import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderTimeInForce;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public class MongoDBManager implements DataManager {
	private static Logger logger = LoggerFactory.getLogger(MongoDBManager.class);
	
	private final MongoDBConnection mongoDBConnection;
	private final MongoCollection<Document> mongoCollection; 
	
	public MongoDBManager(final MongoDBConnection mongoDBConnection, final String collection) {
		this.mongoDBConnection = mongoDBConnection;
		mongoCollection = mongoDBConnection.getMongoDatabase().getCollection(collection);
	}

	@Override
	public void storeOrder(final SimpleOrder order) {
		mongoCollection.replaceOne(new Document("FilledOrder", order.getID()), ConvertSimpleOrderToBSONDocument(order), 
				new UpdateOptions().upsert(true));
		logger.debug(order+" added to collection: "+mongoCollection.toString());
	}
	
	@Override
	public List<SimpleOrder> getOrders(final Optional<OrderType> orderType) {
		long startTime = System.currentTimeMillis();
		final List<SimpleOrder> result = new ArrayList<SimpleOrder>();
		final MongoCursor<Document> cursor = orderType.isPresent() ? mongoCollection.find(new Document("Type", orderType.get().toString())).iterator() : mongoCollection.find().iterator();
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
	
	public static void main(final String[] args) throws NumberFormatException, Exception {
		final Properties p = Utility.getApplicationProperties("tradingMachineServices.properties"); 
		try(final MongoDBManager mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.collection"))) {
			//System.out.println(mongoDBManager.getOrders(Optional.of(OrderType.STOP)).stream().mapToDouble(SimpleOrder::getAvgPx).summaryStatistics());
			System.out.println(mongoDBManager.getOrders(Optional.ofNullable(null)).stream().mapToDouble(SimpleOrder::getAvgPx).summaryStatistics());
			//mongoDBManager.getOrders(Optional.of(OrderType.LIMIT)).stream().map(SimpleOrder::getAvgPx).forEach(System.out::println);
			System.out.println(mongoDBManager.getOrders(Optional.ofNullable(null)).stream().
					collect(Collectors.groupingBy(SimpleOrder::getType, Collectors.counting())));
			
		}
	}
}