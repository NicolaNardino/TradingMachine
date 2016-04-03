package com.projects.tradingMachine.services.simulation.marketData;

import java.util.ArrayList;
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
import com.projects.tradingMachine.utility.marketData.MarketData;
import com.projects.tradingMachine.utility.messaging.ActiveMQProducerConfiguration;

public final class MarketDataProducer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(MarketDataProducer.class);
	
	private final ActiveMQProducerConfiguration activeMQMarketDataProducerConfiguration;
	private final Properties p;
	private static final java.util.Random Random = new java.util.Random();
	
	public MarketDataProducer(final Properties p) throws JMSException {
		this.p = p;
		activeMQMarketDataProducerConfiguration = Utility.createProducer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.marketDataQueue"), DestinationType.Queue);
	}
	
	@Override
	public void run() {
		final List<String> allowedSymbols = Arrays.stream(p.getProperty("allowedSymbols").split(",")).collect(Collectors.toList());
		final ArrayList<MarketData> marketDataList = new ArrayList<MarketData>(allowedSymbols.size());
		while (!Thread.currentThread().isInterrupted()) {
			allowedSymbols.forEach(symbol -> {
				marketDataList.add(new MarketData(symbol, Utility.roundDouble(Random.nextDouble() * 100, 2), Utility.roundDouble(Random.nextDouble() * 100, 2)));
			});
			try {
				activeMQMarketDataProducerConfiguration.getProducer().
				send(activeMQMarketDataProducerConfiguration.getSession().createObjectMessage(marketDataList));
				TimeUnit.SECONDS.sleep(Integer.valueOf(p.getProperty("marketDataPublishingDelay")));
			}
			catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (final Exception e) {
				logger.warn("Unable to produce marked data, due to: "+e.getMessage());
			}
		}
		closeSubscription();
	}

	private void closeSubscription() {
		try {
			if (activeMQMarketDataProducerConfiguration.getConnection() != null) {
				logger.info("Producer JMS Connection closed");
				activeMQMarketDataProducerConfiguration.getConnection().close();
			}	
		}
		catch(final Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void main(final String[] args) throws JMSException, Exception {
		final ExecutorService es = Executors.newFixedThreadPool(1);
		final Future<?> f = es.submit(new MarketDataProducer(Utility.getApplicationProperties("tradingMachineServices.properties")));
		TimeUnit.SECONDS.sleep(10);
		f.cancel(true);
		Utility.shutdownExecutorService(es, 1, TimeUnit.SECONDS);
	}
}