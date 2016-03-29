package com.projects.tradingMachine.services.database.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DatabaseProperties;

public abstract class AbstractDatabaseConnection implements DatabaseConnection {
	private static Logger logger = LoggerFactory.getLogger(AbstractDatabaseConnection.class);
	
	protected final DatabaseProperties databaseProperties;
	private final Connection databaseConnection;
	
	public AbstractDatabaseConnection(final DatabaseProperties databaseProperties) throws ClassNotFoundException, SQLException {
		this.databaseProperties = databaseProperties;
		final String connectionString = getConnectionString();
		logger.info("Connection string: "+connectionString);
		databaseConnection = DriverManager.getConnection(connectionString, databaseProperties.getUserName(), databaseProperties.getPassword());
	}
	
	@Override
	public Connection getConnection() {
		 return databaseConnection;
	}

	@Override
	public void close() throws Exception {
		if (databaseConnection != null) {
			databaseConnection.close();
			logger.info("Connection closed.");
		}
	}
	
	protected abstract String getConnectionString();
}

