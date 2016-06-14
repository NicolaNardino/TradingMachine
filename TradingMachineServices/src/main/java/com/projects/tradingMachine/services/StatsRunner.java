package com.projects.tradingMachine.services;

import static java.util.stream.Collectors.groupingBy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderTimeInForce;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * Extracts statistics out of the filled orders stored to the MongoDB database.
 * 
 * */
public final class StatsRunner implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(StatsRunner.class);
	
	private final Properties properties;
	private final MongoDBManager mongoDBManager;
	
	public StatsRunner(final Properties properties) {
		this.properties = properties;
		mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(properties.getProperty("mongoDB.host"), 
				Integer.valueOf(properties.getProperty("mongoDB.port")), properties.getProperty("mongoDB.database"))), properties.getProperty("mongoDB.executedOrdersCollection"));
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				final List<SimpleOrder> orders = mongoDBManager.getOrders(Optional.ofNullable(null));
				logger.info(groupOrdersByType(orders)+groupOrdersBySide(orders, 5)+groupOrdersBySideTypeTimeInForce(orders)+groupOrdersWithAndWithoutMarketData(orders));
				//testReduction(orders);
				TimeUnit.SECONDS.sleep(Integer.valueOf(properties.getProperty("statsPublishingPeriod")));
			}
			catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (final Exception e) {
				logger.warn("Unable to produce marked data, due to: "+e.getMessage());
			}
		}
		try {
			mongoDBManager.close();
		} catch (final Exception e) {
			logger.warn("Unable to close the MongoDB connection: "+e.getMessage());
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Statistics based on the order type.
	 * */
	private String groupOrdersByType(final List<SimpleOrder> orders) {
		final StringBuilder sb = new StringBuilder("\n");
		final Map<OrderType, List<SimpleOrder>> groupedOrders = orders.parallelStream().collect(Collectors.groupingBy(SimpleOrder::getType));
		for (final Map.Entry<OrderType, List<SimpleOrder>> ot : groupedOrders.entrySet()) {
			final String avgMarketPrice = String.valueOf(Utility.roundDouble(ot.getValue().stream().mapToDouble(SimpleOrder::getAvgPx).average().getAsDouble(), 2)); 
			switch(ot.getKey()) {
				case MARKET: 
					sb.append("Market Orders: \n\t").append("Average price: ").append(avgMarketPrice).append("\n\t");
					break;
				case LIMIT: 
					sb.append("Limit Orders: \n\t").append("Average market price: ").append(avgMarketPrice).append("\n\t").
					append("Average limit price: ").append(String.valueOf(Utility.roundDouble(ot.getValue().stream().mapToDouble(SimpleOrder::getLimit).average().getAsDouble(), 2))).append("\n\t");
					break;
				case STOP: 
					sb.append("Stop Orders: \n\t").append("Average market price: ").append(avgMarketPrice).append("\n\t").
					append("Average stop price: ").append(String.valueOf(Utility.roundDouble(ot.getValue().stream().mapToDouble(SimpleOrder::getStop).average().getAsDouble(), 2))).append("\n\t");
					break;
			}
			sb.append("Average quantity: ").append(String.valueOf(Utility.roundDouble(ot.getValue().stream().mapToDouble(SimpleOrder::getQuantity).average().getAsDouble(), 2))).append("\n\t");
			sb.append("Orders number: ").append(String.valueOf(ot.getValue().size())).append("\n\n");
		}
		return sb.toString();
	}

	/**
	 * Statistics based on the order side.
	 * */
	private String groupOrdersBySide(final List<SimpleOrder> orders, int topOrdersLimit) {
		final StringBuilder sb = new StringBuilder("\n");
		final Map<OrderSide, List<SimpleOrder>> groupedOrders = orders.parallelStream().collect(Collectors.groupingBy(SimpleOrder::getSide));
		final Comparator<SimpleOrder> ordersQuantityComparator = (c1, c2) -> Integer.compare(c1.getQuantity(), c2.getQuantity());
		for (final Map.Entry<OrderSide, List<SimpleOrder>> ot : groupedOrders.entrySet()) {
			switch(ot.getKey()) {
				case BUY: 
					sb.append("BUY orders number: ").append(String.valueOf(ot.getValue().size())).append("\n");
					sb.append("Top "+topOrdersLimit+" biggest quantity BUY orders: \n\t").append(ot.getValue().stream().sorted(ordersQuantityComparator.reversed()).limit(topOrdersLimit).map(so -> so.getSymbol()+"/ "+so.getQuantity()+"/ "+so.getStoreDate()).collect(Collectors.joining("\n\t"))).append("\n\n");
					break;
				case SELL: 
					sb.append("SELL orders number: ").append(String.valueOf(ot.getValue().size())).append("\n");
					sb.append("Top "+topOrdersLimit+" smallest quantity SELL orders: \n\t").append(ot.getValue().stream().
							sorted(Comparator.comparingInt(SimpleOrder::getQuantity)).limit(topOrdersLimit).map(so -> so.getSymbol()+"/ "+so.getQuantity()+"/ "+so.getStoreDate()).collect(Collectors.joining("\n\t"))).append("\n\n");
					//Comparator.comparingInt(SimpleOrder::getQuantity) --> the method reference comparator way.
					break;
			}
		}
		return sb.toString();
	}
	
	/**
	 * Does a three-levels grouping based on side, type and time in force.
	 * */
	private String groupOrdersBySideTypeTimeInForce(final List<SimpleOrder> orders) {
		final StringBuilder sb = new StringBuilder("Orders split based on side, type and time in force.\n umber of\n");
		final Map<OrderSide, Map<OrderType, Map<OrderTimeInForce, Long>>> threeLeveslGroupedOrders = orders.parallelStream().
				collect(groupingBy(SimpleOrder::getSide, groupingBy(SimpleOrder::getType, groupingBy(SimpleOrder::getTimeInForce, Collectors.counting()))));
		for (final Map.Entry<OrderSide, Map<OrderType, Map<OrderTimeInForce, Long>>> threeLeveslGroupedOrdersEntry : threeLeveslGroupedOrders.entrySet()) {
			for (final Map.Entry<OrderType, Map<OrderTimeInForce, Long>> groupedByOrderTypeAndTimeInForceEntry : threeLeveslGroupedOrdersEntry.getValue().entrySet())
				for (final Map.Entry<OrderTimeInForce, Long> groupedByTimeInForceEntry : groupedByOrderTypeAndTimeInForceEntry.getValue().entrySet())
					sb.append("\t"+threeLeveslGroupedOrdersEntry.getKey()+"/ "+ groupedByOrderTypeAndTimeInForceEntry.getKey()+ "/ "+groupedByTimeInForceEntry.getKey()+ " orders: "+groupedByTimeInForceEntry.getValue()+"\n");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	private String groupOrdersWithAndWithoutMarketData(final List<SimpleOrder> orders) {
		final Map<Boolean, Long> groupByMarketDataID = orders.parallelStream().
				collect(Collectors.partitioningBy(so -> so.getMarketDataID() != null, Collectors.counting()));
		return "\nOrders with market data id: "+groupByMarketDataID.get(true)+", without: "+groupByMarketDataID.get(false)+"\n";
	}
	
	public static void main(final String[] args) throws JMSException, Exception {
		final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		final Future<?> f = es.submit(new StatsRunner(Utility.getApplicationProperties("tradingMachineServices.properties")));
		TimeUnit.SECONDS.sleep(60);
		f.cancel(true);
		Utility.shutdownExecutorService(es, 1, TimeUnit.SECONDS);
	}
	
	/**
	 * Tests different ways to do the same reduction. 
	 */
	/*private void testReduction(final List<SimpleOrder> orders) {
		final int quantitySum1 = orders.parallelStream().mapToInt(SimpleOrder::getQuantity).reduce(0, (so1, so2) -> so1 + so2);
		final int quantitySum2 = orders.parallelStream().mapToInt(SimpleOrder::getQuantity).reduce(0, Integer::sum);
		final int quantitySum3 = orders.parallelStream().collect(Collectors.reducing(0, SimpleOrder::getQuantity, Integer::sum));
		final Integer quantitySum4 = orders.parallelStream().collect(Collectors.summingInt(SimpleOrder::getQuantity));
		final int quantitySum5 = orders.parallelStream().mapToInt(SimpleOrder::getQuantity).sum();
		System.out.println(quantitySum1+"/ "+quantitySum2+"/ "+quantitySum3+"/"+quantitySum4+"/ "+quantitySum5);
	}*/
}