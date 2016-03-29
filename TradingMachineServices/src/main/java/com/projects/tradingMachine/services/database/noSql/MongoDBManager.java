package com.projects.tradingMachine.services.database.noSql;

import java.util.Date;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.model.UpdateOptions;
import com.projects.tradingMachine.services.database.DataManager;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public class MongoDBManager implements DataManager {
	private static Logger logger = LoggerFactory.getLogger(MongoDBManager.class);
	
	private final MongoDBConnection mongoDBConnection;
	private final String collection;
	
	public MongoDBManager(final MongoDBConnection mongoDBConnection, final String collection) {
		this.mongoDBConnection = mongoDBConnection;
		this.collection = collection;
	}

	@Override
	public void storeOrder(final SimpleOrder order) {
		mongoDBConnection.getMongoDatabase().getCollection(collection).
		replaceOne(new Document("FilledOrder", order.getID()), ConvertSimpleOrderToBSONDocument(order), 
				new UpdateOptions().upsert(true));
		logger.debug(order+" added to collection: "+collection);
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

	@Override
	public void close() throws Exception {
		mongoDBConnection.close();
	}
}