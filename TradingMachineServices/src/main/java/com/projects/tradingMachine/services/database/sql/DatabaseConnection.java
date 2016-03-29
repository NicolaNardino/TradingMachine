package com.projects.tradingMachine.services.database.sql;

import java.sql.Connection;

public interface DatabaseConnection extends AutoCloseable {
	
	Connection getConnection();
}