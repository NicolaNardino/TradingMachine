package com.projects.tradingMachine.services.database.noSql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.projects.tradingMachine.services.database.DatabaseProperties;

public final class MongoDBConnection implements AutoCloseable {
	private static Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);
	
	private final MongoClient mongoClient;
	private final MongoDatabase mongoDatabase;
	
	public MongoDBConnection(final DatabaseProperties databaseProperties) {
		mongoClient = new MongoClient(databaseProperties.getHost(), databaseProperties.getPort());
		mongoDatabase = mongoClient.getDatabase(databaseProperties.getDatabaseName());
	}
	
	@Override
	public void close() throws Exception {
		if (mongoClient != null) {
			mongoClient.close();
			logger.info("Connection closed.");
		}	
	}
	
	public MongoDatabase getMongoDatabase() {
		return mongoDatabase;
	}
}