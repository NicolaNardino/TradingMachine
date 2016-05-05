package com.projects.tradingMachine.services.simulation.orders;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.TradingMachineMessageProducer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;

public final class OrdersProducer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(OrdersProducer.class);
	
	private final Properties properties;
	private final TradingMachineMessageProducer ordersProducer;
	
	public OrdersProducer(final Properties properties) throws JMSException {
		this.properties = properties;
		ordersProducer = new TradingMachineMessageProducer(properties.getProperty("activeMQ.url"), properties.getProperty("activeMQ.ordersQueue"), DestinationType.Queue, "OrdersProducer", null);
		ordersProducer.start();
	}
	
	@Override
	public void run() {
		final List<String> allowedSymbols = Arrays.stream(properties.getProperty("allowedSymbols").split(",")).collect(Collectors.toList());
		while (!Thread.currentThread().isInterrupted()) {
        	try {
        		ordersProducer.getProducer().send(ordersProducer.getSession().createObjectMessage(RandomOrdersBuilder.build(allowedSymbols)));
				TimeUnit.SECONDS.sleep(Integer.valueOf(properties.getProperty("ordersPublishingPeriod")));
			} 
        	catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
        	catch (final Exception e) {
				logger.warn("Failed to produce order, due to: "+e.getMessage());
			}
        }
		//close subscription.
		try {
			ordersProducer.stop();
		}
		catch(final Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void main(final String[] args) throws JMSException, Exception {
		final ExecutorService es = Executors.newFixedThreadPool(1);
		final Future<?> f = es.submit(new OrdersProducer(Utility.getApplicationProperties("tradingMachineServices.properties")));
		TimeUnit.SECONDS.sleep(10);
		f.cancel(true);
		Utility.shutdownExecutorService(es, 1, TimeUnit.SECONDS);
	}
}