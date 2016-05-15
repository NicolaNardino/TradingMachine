package com.projects.tradingMachine.utility.marketData;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.Utility;

/**
 * This can be used to produce market in-place data updates, i.e., without relying on to the queue-based infrastructure. 
 * */
public final class MarketDataManagerInPlaceUpdater {
	private static final Logger logger = LoggerFactory.getLogger(MarketDataManagerInPlaceUpdater.class);
	
	private final ScheduledExecutorService scheduler;
	private final ConcurrentMap<String, MarketData> marketDataRepository;
	private final List<String> allowedSymbols;
	private ScheduledFuture<?> marketDataUpdateFuture;
	
	public MarketDataManagerInPlaceUpdater(final List<String> allowedSymbols) {
		this.allowedSymbols = allowedSymbols;
		marketDataRepository = new ConcurrentHashMap<>();
		scheduler = Executors.newScheduledThreadPool(1);
		((ScheduledThreadPoolExecutor)scheduler).setRemoveOnCancelPolicy(true);
	}
	
	public MarketData get(final String symbol) {
		return marketDataRepository.getOrDefault(symbol, Utility.buildRandomMarketDataItem(symbol));
	}
	
	public void startUpdates() {
		marketDataUpdateFuture = scheduler.scheduleWithFixedDelay(() -> {
			Thread.currentThread().setName("MarketDataUpdater-Thread");
			allowedSymbols.forEach(symbol -> {
				final MarketData marketDataValue = Utility.buildRandomMarketDataItem(symbol);
				marketDataRepository.merge(symbol, marketDataValue, (oldValue, newValue) -> marketDataValue);
			});
			Thread.currentThread().setName("MarketDataUpdater-Thread");
		}, 500, 400, TimeUnit.MILLISECONDS);
	}
	
	public void stopUpdates() throws InterruptedException {
		marketDataUpdateFuture.cancel(false);
		logger.info("Cancelled scheduled future");
		scheduler.shutdown();
		if (!scheduler.awaitTermination(10, TimeUnit.SECONDS))
			scheduler.shutdownNow();
		logger.info("Terminated ScheduledExecutorService");
	}
	
}
