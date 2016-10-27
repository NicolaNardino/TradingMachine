package com.projects.tradingMachine.utility.database;

public final class DbUtility {

	public static String getMySqlConnectionUrl(final DatabaseProperties dbProperties) {
		return "jdbc:mysql://"+dbProperties.getHost()+"/"+dbProperties.getDatabaseName();
	}
}
