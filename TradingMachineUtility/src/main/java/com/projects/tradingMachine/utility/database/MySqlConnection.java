package com.projects.tradingMachine.utility.database;

import java.sql.SQLException;

public class MySqlConnection extends AbstractDatabaseConnection {
	
	public MySqlConnection(final DatabaseProperties databaseProperties) throws ClassNotFoundException, SQLException {
		super(databaseProperties);
	}

	@Override
	protected String getConnectionString() {
		return DbUtility.getMySqlConnectionUrl(databaseProperties);
	}
}
