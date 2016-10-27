package com.projects.tradingMachine.utility.marketData;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import java.util.stream.IntStream;

import com.projects.tradingMachine.utility.Utility;

public final class MarketData implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String symbol;
	private final double bid; 
	private final double ask;
	private final int bidSize;
	private final int askSize;
	private final Date quoteDateTime;
	private volatile String id;
	
	/**
	 * This is used to build a new instance.
	 * */
	public MarketData(final String symbol, final double bid, final double ask, final int bidSize, final int askSize) {
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.bidSize = bidSize;
		this.askSize = askSize;
		quoteDateTime = new Date();
	}

	/**
	 * This is used to set a new instance when loaded from a back-end store.
	 * */
	public MarketData(final String id, final String symbol, final double bid, final double ask, final int bidSize, final int askSize, final Date quoteDateTime) {
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.bidSize = bidSize;
		this.askSize = askSize;
		this.quoteDateTime = quoteDateTime;
		this.id = id;
	}
	
	public String getID() {
		if (id == null)
			id = UUID.randomUUID().toString();
		return id; 
	}

	public String getSymbol() {
		return symbol;
	}


	public double getBid() {
		return bid;
	}

	public double getAsk() {
		return ask;
	}


	public int getBidSize() {
		return bidSize;
	}


	public int getAskSize() {
		return askSize;
	}

	public Date getQuoteTime() {
		return quoteDateTime;
	}

	@Override
	public String toString() {
		return "MarketData [id = "+id+", symbol=" + symbol + ", bid=" + bid + ", ask=" + ask + ", bidSize=" + bidSize + ", askSize="
				+ askSize + ", quoteTime=" + quoteDateTime + "]";
	}
	
	public static void main(final String[] args) {
		IntStream.range(1, 100).forEach(i -> System.out.println(Utility.buildRandomMarketDataItem("ABC").getID()));
	}
}
