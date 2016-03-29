package com.projects.tradingMachine.services.simulation.orders;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
		while (!Thread.currentThread().isInterrupted()) {
        	try {
        		activeMQOrdersProducerConfiguration.getProducer().send(activeMQOrdersProducerConfiguration.getSession().createObjectMessage(RandomOrdersBuilder.build()));
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
		Thread.sleep(10000);
		f.cancel(true);
		Utility.shutdownExecutorService(es, 1, TimeUnit.SECONDS);
	}
}