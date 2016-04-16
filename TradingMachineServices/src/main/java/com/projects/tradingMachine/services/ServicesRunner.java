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
import com.projects.tradingMachine.utility.Utility;

/**
 * Starts all the support services, namely:
 * 
 * <ul>
 * 	<li>MarketDataProducer: it builds random ask and bid prices for a selected range of symbols and sends them to a queue, every X seconds.</li>
 *  <li>OrdersProducer: it builds random buy/ sell market, limit and stop orders and sends them to a queue, every X seconds.</li>
 *  <li>FilledOrdersBackEndStore: it subscribes to the FilledOrdersTopic to receive fully filled orders and stores them to MySQL and MongDB databases.</li>
 * </ul>
 * */
public class ServicesRunner {

	private final ExecutorService es;
	private final FilledOrdersBackEndStore filledOrdersBackEndStore;
	private final Future<?> ordersProducerFuture;
	private final Future<?> marketDataProducerFuture;
	
	/**
	 * Starts all three services. Specifically, it starts the OrdersProducer and MarketDataManager by a thread pool returning future, which will be
	 * later used to stop the services by sending an interrupt.
	 * */
	public ServicesRunner() throws ClassNotFoundException, JMSException, SQLException, FileNotFoundException, IOException {
		final Properties p = Utility.getApplicationProperties("tradingMachineServices.properties");
		es = Executors.newFixedThreadPool(2);
		filledOrdersBackEndStore = new FilledOrdersBackEndStore(p);
		filledOrdersBackEndStore.start();
		ordersProducerFuture = es.submit(new OrdersProducer(p));
		marketDataProducerFuture = es.submit(new MarketDataProducer(p));
	}
	
	/**
	 * Stops all services.
	 * */
	public void stop() throws Exception {
		try {
			filledOrdersBackEndStore.stop();
			ordersProducerFuture.cancel(true);
			marketDataProducerFuture.cancel(true);	
		}
		finally {
			Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS); //thread pool gets shut down by ExecutorService.shutdown, not shutdownNow which would have cancelled by running tasks.	
		}
	}
	
	public static void main(String[] args) throws Exception {
		new ServicesRunner();
		//TimeUnit.SECONDS.sleep(10);
		//sr.stop();
	}
}
