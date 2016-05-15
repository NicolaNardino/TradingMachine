package com.projects.tradingMachine.utility.marketData;

import java.io.Serializable;
import java.util.Date;

public final class MarketData implements Serializable{

	private static final long serialVersionUID = 1L;

	private final String symbol;
	private final double bid; 
	private final double ask;
	private final int bidSize;
	private final int askSize;
	private final Date quoteDateTime;
	
	public MarketData(final String symbol, final double bid, final double ask, final int bidSize, final int askSize) {
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.bidSize = bidSize;
		this.askSize = askSize;
		quoteDateTime = new Date();
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(ask);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + askSize;
		temp = Double.doubleToLongBits(bid);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + bidSize;
		result = prime * result + ((quoteDateTime == null) ? 0 : quoteDateTime.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarketData other = (MarketData) obj;
		if (Double.doubleToLongBits(ask) != Double.doubleToLongBits(other.ask))
			return false;
		if (askSize != other.askSize)
			return false;
		if (Double.doubleToLongBits(bid) != Double.doubleToLongBits(other.bid))
			return false;
		if (bidSize != other.bidSize)
			return false;
		if (quoteDateTime == null) {
			if (other.quoteDateTime != null)
				return false;
		} else if (!quoteDateTime.equals(other.quoteDateTime))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MarketData [symbol=" + symbol + ", bid=" + bid + ", ask=" + ask + ", bidSize=" + bidSize + ", askSize="
				+ askSize + ", quoteTime=" + quoteDateTime + "]";
	}
}
