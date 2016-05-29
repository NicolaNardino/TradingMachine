package com.projects.tradingMachine.tradeMonitor.util;

public final class MarketDataSummary {
	private final String Symbol;
	private final double AvgBid;
	private final double AvgAsk;
	private final double AvgBidSize;
	private final double AvgAskSize;
	private final long ItemsNumber;
	
	public MarketDataSummary(final String symbol, final double avgBid, final double avgAsk, final double avgBidSize, final double avgAskSize, final long itemsNumber) {
		Symbol = symbol;
		AvgBid = avgBid;
		AvgAsk = avgAsk;
		AvgBidSize = avgBidSize;
		AvgAskSize = avgAskSize;
		ItemsNumber = itemsNumber;
	}

	public String getSymbol() {
		return Symbol;
	}

	public double getAvgBid() {
		return AvgBid;
	}

	public double getAvgAsk() {
		return AvgAsk;
	}

	public double getAvgBidSize() {
		return AvgBidSize;
	}

	public double getAvgAskSize() {
		return AvgAskSize;
	}

	public double getItemsNumber() {
		return ItemsNumber;
	}

	@Override
	public String toString() {
		return "MarketDataSummary [Symbol=" + Symbol + ", AvgBid=" + AvgBid + ", AvgAsk=" + AvgAsk + ", AvgBidSize="
				+ AvgBidSize + ", AvgAskSize=" + AvgAskSize + ", ItemsNumber=" + ItemsNumber + "]";
	}
}
