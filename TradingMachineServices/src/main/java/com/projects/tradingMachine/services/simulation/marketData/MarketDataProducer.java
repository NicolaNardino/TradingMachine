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

import com.projects.tradingMachine.utility.TradingMachineMessageProducer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.marketData.MarketData;

public final class MarketDataProducer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(MarketDataProducer.class);
	
	private final TradingMachineMessageProducer marketDataProducer;
	private final Properties properties;
	private static final java.util.Random Random = new java.util.Random();
	
	public MarketDataProducer(final Properties properties) throws JMSException {
		this.properties = properties;
		marketDataProducer = new TradingMachineMessageProducer(properties.getProperty("activeMQ.url"), properties.getProperty("activeMQ.marketDataQueue"), DestinationType.Queue, null);
		marketDataProducer.start();
	}
	
	@Override
	public void run() {
		final List<String> allowedSymbols = Arrays.stream(properties.getProperty("allowedSymbols").split(",")).collect(Collectors.toList());
		final ArrayList<MarketData> marketDataList = new ArrayList<MarketData>(allowedSymbols.size());
		while (!Thread.currentThread().isInterrupted()) {
			allowedSymbols.forEach(symbol -> {
				marketDataList.add(new MarketData(symbol, Utility.roundDouble(Random.nextDouble() * 100, 2), Utility.roundDouble(Random.nextDouble() * 100, 2)));
			});
			try {
				marketDataProducer.getProducer().
				send(marketDataProducer.getSession().createObjectMessage(marketDataList));
				TimeUnit.SECONDS.sleep(Integer.valueOf(properties.getProperty("marketDataPublishingPeriod")));
			}
			catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (final Exception e) {
				logger.warn("Unable to produce marked data, due to: "+e.getMessage());
			}
		}
		//stop producer.
		try {
			marketDataProducer.stop();	
		}
		catch(final Exception ex) {
			logger.warn(ex.getMessage());
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