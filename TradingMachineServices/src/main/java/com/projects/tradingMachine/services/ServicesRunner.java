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

public class ServicesRunner {

	private final ExecutorService es;
	private final FilledOrdersBackEndStore filledOrdersBackEndStore;
	private final Future<?> ordersProducerFuture;
	private final Future<?> marketDataProducerFuture;
	
	public ServicesRunner() throws ClassNotFoundException, JMSException, SQLException, FileNotFoundException, IOException {
		final Properties p = Utility.getApplicationProperties("tradingMachineServices.properties");
		es = Executors.newFixedThreadPool(2);
		filledOrdersBackEndStore = new FilledOrdersBackEndStore(p);
		ordersProducerFuture = es.submit(new OrdersProducer(p));
		marketDataProducerFuture = es.submit(new MarketDataProducer(p));
	}
	
	public void stop() throws Exception {
		filledOrdersBackEndStore.closeSubscription();
		ordersProducerFuture.cancel(true);
		marketDataProducerFuture.cancel(true);
		Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS); 
		//thread pool gets shut down by ExecutorService.shutdown, not shutdownNow which would have cancelled by running tasks.
	}
	
	public static void main(String[] args) throws Exception {
		new ServicesRunner();
		//Thread.sleep(10000);
		//sr.stop();
	}
}
