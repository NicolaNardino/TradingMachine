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

import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.messaging.ActiveMQProducerConfiguration;

public final class OrdersProducer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(OrdersProducer.class);

	private final ActiveMQProducerConfiguration activeMQOrdersProducerConfiguration;
	private final Properties p;
	
	public OrdersProducer(final Properties p) throws JMSException {
		this.p = p;
		activeMQOrdersProducerConfiguration = Utility.createProducer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.ordersQueue"), DestinationType.Queue);
	}
	
	@Override
	public void run() {
		final List<String> allowedSymbols = Arrays.stream(p.getProperty("allowedSymbols").split(",")).collect(Collectors.toList());
		while (!Thread.currentThread().isInterrupted()) {
        	try {
        		activeMQOrdersProducerConfiguration.getProducer().send(activeMQOrdersProducerConfiguration.getSession().createObjectMessage(RandomOrdersBuilder.build(allowedSymbols)));
				TimeUnit.SECONDS.sleep(Integer.valueOf(p.getProperty("ordersPublishingDelay")));
			} 
        	catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
        	catch (final Exception e) {
				logger.warn("Failed to produce order, due to: "+e.getMessage());
			}
        }
		closeSubscription();
	}

	public void closeSubscription() {
		try {
			if (activeMQOrdersProducerConfiguration.getConnection() != null) {
				logger.info("Producer JMS Connection closed");
				activeMQOrdersProducerConfiguration.getConnection().close();
			}
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