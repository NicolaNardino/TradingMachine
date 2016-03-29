package com.projects.tradingMachine.orderRouter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class OrderManager {

	private final ConcurrentMap<String, SimpleOrder> orders;
	
	public OrderManager() {
		orders = new ConcurrentHashMap<>();
	}
	
	public void add(final SimpleOrder order) {
		orders.putIfAbsent(order.getID(), order);
	}
	
	public SimpleOrder getOrder(final String orderId) {
		return orders.get(orderId);
	}
	
	public void updateOrder(final SimpleOrder order) {
		orders.put(order.getID(), order);
	}
}