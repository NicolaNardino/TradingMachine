package com.projects.tradingMachine.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import com.projects.tradingMachine.services.database.FilledOrdersBackEndStore;
import com.projects.tradingMachine.services.simulation.marketData.MarketDataProducer;
import com.projects.tradingMachine.services.simulation.orders.OrdersProducer;
import com.projects.tradingMachine.utility.ServiceLifeCycle;
import com.projects.tradingMachine.utility.Utility;

/**
 * Starts all the support services, namely:
 * 
 * <ul>
 * 	<li>MarketDataProducer: it builds random ask and bid prices for a selected range of symbols and sends them to a queue, every X seconds.</li>
 *  <li>OrdersProducer: it builds random buy/ sell market, limit and stop orders and sends them to a queue, every X seconds.</li>
 *  <li>FilledOrdersBackEndStore: it subscribes to the FilledOrdersTopic to receive fully filled orders and stores them to MySQL and MongDB databases.</li>
 *  <li>StatsRunner: prints some order execution statistics.</li>
 * </ul>
 * */
public final class ServicesRunner implements ServiceLifeCycle {

	private final ExecutorService es;
	private final FilledOrdersBackEndStore filledOrdersBackEndStore;
	private final Properties properties;
	private Future<?> ordersProducerFuture;
	private Future<?> marketDataProducerFuture;
	private Future<?> statsRunnerFuture;
	
	/**
	 * Sets up the executor service with 3 threads for OrdersProducer, MarketDataProducer and StatsRunner.
	 * */
	public ServicesRunner(final Properties properties) throws ClassNotFoundException, JMSException, SQLException, FileNotFoundException, IOException {
		this.properties = properties;
		es = Executors.newFixedThreadPool(3);
		filledOrdersBackEndStore = new FilledOrdersBackEndStore(properties);
	}
	
	/**
	 * Starts all services.
	 * */
	@Override
	public void start() throws Exception {
		ordersProducerFuture = es.submit(new OrdersProducer(properties));
		marketDataProducerFuture = es.submit(new MarketDataProducer(properties));
		statsRunnerFuture = es.submit(new StatsRunner(properties));
		filledOrdersBackEndStore.start();
	}
	
	/**
	 * Stops all services.
	 * */
	@Override
	public void stop() throws Exception {
		try {
			filledOrdersBackEndStore.stop();
			if (ordersProducerFuture != null)
				ordersProducerFuture.cancel(true);
			if (marketDataProducerFuture != null)
				marketDataProducerFuture.cancel(true);
			if (statsRunnerFuture != null)
				statsRunnerFuture.cancel(true);
		}
		finally {
			Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS); //thread pool gets shut down by ExecutorService.shutdown, not shutdownNow which would have cancelled by running tasks.	
		}
	}
	
	public static void main(String[] args) throws Exception {
		final ServicesRunner servicesRunner = new ServicesRunner(Utility.getApplicationProperties("tradingMachineServices.properties"));
		servicesRunner.start();
		//TimeUnit.SECONDS.sleep(10);
		//servicesRunner.stop();
	}
}
