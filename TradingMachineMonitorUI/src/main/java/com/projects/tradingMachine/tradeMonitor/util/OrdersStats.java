package com.projects.tradingMachine.tradeMonitor.util;

public final class OrdersStats {
	private final long total;
	private final long filled;
	private final long rejected;
	private final long buy;
	private final long sell;
	private final long market;
	private final long limit;
	private final long stop;
	
	public OrdersStats(final long total, final long filled, final long rejected, final long buy, final long sell, final long market, final long limit, final long stop) {
		this.total = total;
		this.filled = filled;
		this.rejected = rejected;
		this.buy = buy;
		this.sell = sell;
		this.market = market;
		this.limit = limit;
		this.stop = stop;
	}

	public long getTotal() {
		return total;
	}

	public long getFilled() {
		return filled;
	}

	public long getRejected() {
		return rejected;
	}

	public long getBuy() {
		return buy;
	}

	public long getSell() {
		return sell;
	}

	public long getMarket() {
		return market;
	}

	public long getLimit() {
		return limit;
	}

	public long getStop() {
		return stop;
	}
	
}
