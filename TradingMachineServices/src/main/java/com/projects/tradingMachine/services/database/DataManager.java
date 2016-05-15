package com.projects.tradingMachine.services.database;

import java.util.List;
import java.util.Optional;

import com.projects.tradingMachine.utility.marketData.MarketData;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public interface DataManager extends AutoCloseable {
	
	void storeOrder(SimpleOrder order);
	
	List<SimpleOrder> getOrders(Optional<OrderType> orderType);
	
	void addMarketDataItems(List<MarketData> marketDataItems, boolean deleteFirst);
}
