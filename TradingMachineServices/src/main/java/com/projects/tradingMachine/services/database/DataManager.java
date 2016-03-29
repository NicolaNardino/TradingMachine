package com.projects.tradingMachine.services.database;

import com.projects.tradingMachine.utility.order.SimpleOrder;

public interface DataManager extends AutoCloseable {
	void storeOrder(SimpleOrder order);
}
