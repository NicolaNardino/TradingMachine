package com.projects.tradingMachine.utility.database;

public class DatabaseProperties {
	private final int port;
	private final String host;
	private final String databaseName;
	private final String userName;
	private final String password;
	
	public DatabaseProperties(final String host, final int port, final String databaseName, final String userName, final String password) {
		this.port = port;
		this.host = host;
		this.databaseName = databaseName;
		this.userName = userName;
		this.password = password;
	}
	
	public DatabaseProperties(final String host, final int port, final String databaseName) {
		this(host, port, databaseName, null, null);
	}

	public final int getPort() {
		return port;
	}

	public final String getUserName() {
		return userName;
	}

	public final String getPassword() {
		return password;
	}

	public final String getHost() {
		return host;
	}
	
	public final String getDatabaseName() {
		return databaseName;
	}
}
