package com.projects.tradingMachine.services.database.sql;

import java.sql.SQLException;

import com.projects.tradingMachine.services.database.DatabaseProperties;

public class MySqlConnection extends AbstractDatabaseConnection {
	
	public MySqlConnection(final DatabaseProperties databaseProperties) throws ClassNotFoundException, SQLException {
		super(databaseProperties);
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:mysql://"+databaseProperties.getHost()+"/"+databaseProperties.getDatabaseName();
	}
}
