package com.projects.tradingMachine.utility.database;

import java.sql.Connection;

public interface DatabaseConnection extends AutoCloseable {
	
	Connection getConnection();
}