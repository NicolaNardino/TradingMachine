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

import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.utility.TradingMachineMessageProducer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.marketData.MarketData;

/**
 * Randomly builds market data items, publishes them to the activeMQ.marketDataQueue and stores them mongoDB.marketDataCollection.
 * */
public final class MarketDataProducer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(MarketDataProducer.class);
	
	private final TradingMachineMessageProducer marketDataProducer;
	private final MongoDBManager mongoDBManager;
	private final ExecutorService executorService;
	private final Properties properties;
	
	public MarketDataProducer(final Properties properties) throws JMSException {
		this.properties = properties;
		marketDataProducer = new TradingMachineMessageProducer(properties.getProperty("activeMQ.url"), properties.getProperty("activeMQ.marketDataQueue"), DestinationType.Queue, "MarketDataProducer", null);
		marketDataProducer.start();
		mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(properties.getProperty("mongoDB.host"), 
				Integer.valueOf(properties.getProperty("mongoDB.port")), properties.getProperty("mongoDB.database"))), 
				properties.getProperty("mongoDB.filledOrdersCollection"), properties.getProperty("mongoDB.marketDataCollection"));
		executorService = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void run() {
		final List<String> allowedSymbols = Arrays.stream(properties.getProperty("allowedSymbols").split(",")).collect(Collectors.toList());
		while (!Thread.currentThread().isInterrupted()) {
			final ArrayList<MarketData> marketDataItems = new ArrayList<MarketData>(allowedSymbols.size());
			allowedSymbols.forEach(symbol ->  marketDataItems.add(Utility.buildRandomMarketDataItem(symbol)));
			try {
				marketDataProducer.getProducer().
				send(marketDataProducer.getSession().createObjectMessage(marketDataItems));
				executorService.execute(() -> mongoDBManager.addMarketDataItems(marketDataItems, false));
				TimeUnit.SECONDS.sleep(Integer.valueOf(properties.getProperty("marketDataPublishingPeriod")));
			}
			catch(final InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			catch (final Exception e) {
				logger.warn("Unable to produce marked data, due to: "+e.getMessage());
			}
		}
		cleanUp();
	}
	
	private void cleanUp() {
		try {
			marketDataProducer.stop();	
			mongoDBManager.close();
			Utility.shutdownExecutorService(executorService, 1, TimeUnit.SECONDS);
		}
		catch(final Exception ex) {
			logger.warn(ex.getMessage());
			throw new RuntimeException(ex);
		}
	}
	
	public static void main(final String[] args) throws JMSException, Exception {
		final ExecutorService es = Executors.newFixedThreadPool(1);
		final Future<?> f = es.submit(new MarketDataProducer(Utility.getApplicationProperties("tradingMachineServices.properties")));
		TimeUnit.SECONDS.sleep(100000);
		f.cancel(true);
		Utility.shutdownExecutorService(es, 1, TimeUnit.SECONDS);
	}
}