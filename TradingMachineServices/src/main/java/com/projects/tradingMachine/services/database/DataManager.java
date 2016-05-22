package com.projects.tradingMachine.services.database;

import java.util.List;
import java.util.Optional;

import com.projects.tradingMachine.utility.marketData.MarketData;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public interface DataManager extends AutoCloseable {
	
	/**
	 * Stores an order in the back-end.
	 * 
	 * @param order Order to store.
	 */
	void storeOrder(SimpleOrder order);
	
	/**
	 * Gets the orders from the the back-end.
	 * 
	 * @param orderType If passed, then it only retrieves orders with the given type.
	 * 
	 * @return List of orders.
	 */
	List<SimpleOrder> getOrders(Optional<OrderType> orderType);
	
	/**
	 * Stores a list of market data items in the back-end.
	 * 
	 * @param marketDataItems List of market data items.
	 * @param deleteFirst If true, then  it deletes all market data items before adding the ones passed in.
	 */
	void storeMarketDataItems(List<MarketData> marketDataItems, boolean deleteFirst);

	/**
	 * Gets the market data from the back-end.
	 * 
	 * @param symbol If passed, then it only retrieves market data with the given symbol.
	 * 
	 * @return List of market data items.
	 */
	List<MarketData> getMarketData(Optional<String> symbol);
}
