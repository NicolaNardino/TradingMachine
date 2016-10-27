package com.projects.tradingMachine.utility.database.creditCheck;

import java.sql.SQLException;

public interface ICreditCheck {

	boolean hasEnoughCredit(double credit);
	
	void setCredit(double credit);
	
	void closeConnection() throws SQLException;
}
