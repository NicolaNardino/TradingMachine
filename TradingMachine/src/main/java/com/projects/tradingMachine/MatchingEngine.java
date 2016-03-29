package com.projects.tradingMachine;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.Utility;

import quickfix.FieldNotFound;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;

/**
 * Given an order and a link to the market data manager, it tries to match limit and stop orders based on the current market data. 
 * It always fills market orders.
 * The market data provides bid and ask prices of a given symbol, not quantities. Which means, orders will be filled, if possible, completely. 
 * */
public final class MatchingEngine implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);
	private static final AtomicInteger orderIdSequence = new AtomicInteger(0);
    private static final AtomicInteger execIdSequence = new AtomicInteger(0);
    private final MarketDataManager marketDataManager;
	private final quickfix.fix50.NewOrderSingle order;
	private final SessionID sessionID;
    
	public MatchingEngine(final MarketDataManager marketDataManager, final quickfix.fix50.NewOrderSingle order, final SessionID sessionID) {
		this.marketDataManager = marketDataManager;
		this.order = order;
		this.sessionID = sessionID;
	}
	
	@Override
	public void run() {
		try {
            final OrderQty orderQty = order.getOrderQty();
            final quickfix.fix50.ExecutionReport accept = new quickfix.fix50.ExecutionReport(
            		buildOrderID(), buildExecID(), new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.NEW), order.getSide(), 
            		new LeavesQty(order.getOrderQty().getValue()), new CumQty(0));
            accept.set(order.getClOrdID());
            accept.set(order.getSymbol());
            Utility.sendMessage(sessionID, accept);
            //try to fill now.
            final Double fillPrice = findFillPrice(order, 100);
            if (fillPrice != null) {
            	final quickfix.fix50.ExecutionReport executionReport = new quickfix.fix50.ExecutionReport(
                        buildOrderID(), buildExecID(), new ExecType(ExecType.FILL), new OrdStatus(
                                OrdStatus.FILLED), order.getSide(), new LeavesQty(0), new CumQty(
                                orderQty.getValue()));
                executionReport.set(order.getClOrdID());
                executionReport.set(order.getSymbol());
                executionReport.set(orderQty);
                executionReport.set(new LastQty(orderQty.getValue()));
                executionReport.set(new LastPx(fillPrice));
                executionReport.set(new AvgPx(fillPrice));
                Utility.sendMessage(sessionID, executionReport);	
            }
        } catch (final Exception e) {
            LogUtil.logThrowable(sessionID, e.getMessage(), e);
        }	
	}
	
	private Double findFillPrice(final quickfix.fix50.NewOrderSingle order, final int nrTrials) throws FieldNotFound, InterruptedException {
		int counter = 0;
		switch(order.getChar(OrdType.FIELD)) {
		case OrdType.LIMIT: 
			final double limitPrice = order.getDouble(Price.FIELD);
        	//loop until the limit order price is executable.
        	final char limitOrderSide = order.getChar(Side.FIELD);
        	while(counter < nrTrials) {
        		final double marketPrice = getMarketPrice(order);
        		if ((limitOrderSide == Side.BUY && Double.compare(marketPrice, limitPrice) <= 0)
                        || (limitOrderSide == Side.SELL && Double.compare(marketPrice, limitPrice) >= 0)) {
        			log.info("Found filling price for limit order: "+marketPrice+", limit price: "+limitPrice);
        			return marketPrice;
        		}
        		else {
        			log.debug("Looping to find filling price for limit order, market price: "+marketPrice+", limit price: "+limitPrice);
        			Thread.sleep(500);
        			counter++;
        		}
        	}
        	return null; //price not found.
		case OrdType.STOP: 
			final double stopPrice = order.getDouble(StopPx.FIELD);
    		//loop until the limit order price is executable.
        	final char stopOrderPrice = order.getChar(Side.FIELD);
        	while(counter < nrTrials) {
        		final double marketPrice = getMarketPrice(order);
        		if ((stopOrderPrice == Side.BUY && Double.compare(marketPrice, stopPrice) > 0)
                        || (stopOrderPrice == Side.SELL && Double.compare(marketPrice, stopPrice) < 0)) {
        			log.info("Found filling price for stop order: "+marketPrice+", limit price: "+stopPrice);
        			return marketPrice;
        		}
        		else {
        			log.debug("Looping to find filling price for stop order, market price: "+marketPrice+", limit price: "+stopPrice);
        			Thread.sleep(500);
        			counter++;
        		}
        	}
        	return null; //price not found.
        	default: return getMarketPrice(order);
		}
    }
	
	private double getMarketPrice(final Message message) throws FieldNotFound {
		switch (message.getChar(Side.FIELD)) {
		case Side.BUY:
			return marketDataManager.get(message.getString(Symbol.FIELD)).getAsk();
		case Side.SELL:
			return marketDataManager.get(message.getString(Symbol.FIELD)).getBid();
		default:
			throw new RuntimeException("Invalid order side: " + message.getChar(Side.FIELD));
		}
	}
	
	private static OrderID buildOrderID() {
        return new OrderID(String.valueOf(orderIdSequence.incrementAndGet()));
    }

    private static ExecID buildExecID() {
        return new ExecID(String.valueOf(execIdSequence.incrementAndGet()));
    }
}