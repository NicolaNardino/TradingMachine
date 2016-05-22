package com.projects.tradingMachine.tradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.table.AbstractTableModel;

import com.projects.tradingMachine.utility.marketData.MarketData;

/**
 * Custom table model with the initial data set coming from the market data stored in a MongoDB collection, then incremented with updates coming from the topic 
 * MarketDataTopic. 
 * */
public final class MarketDataTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] columnNames = {"ID", "Symbol",  "Bid", "Ask", "BidSize", "AskSize", "QuoteTime"};
    
    private final List<MarketData> marketDataItems;
    
    public MarketDataTableModel(final List<MarketData> marketDataItems) throws FileNotFoundException, IOException, JMSException {
    	super();
    	this.marketDataItems = marketDataItems;
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return marketDataItems.size();
    }

    @Override
    public String getColumnName(final int colId) {
        return columnNames[colId];
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
    	if (marketDataItems.size() == 0)
    		return null;
        final MarketData marketData = marketDataItems.get(rowId);
        switch (colId) {
            case 0:
                value = marketData.getID();
                break;
            case 1:
                value = marketData.getSymbol();
                break;
            case 2:
                value = marketData.getBid();
                break;
            case 3:
                value = marketData.getAsk();
                break;
            case 4:
                value = marketData.getBidSize();
                break;
            case 5:
                value = marketData.getAskSize();
                break;
            case 6:
                value = marketData.getQuoteTime();
                break;
        }
        return value;
    }

    @Override
    public Class<?> getColumnClass(final int c) {
    	final Object valueAt = getValueAt(0, c);
    	if (valueAt == null)
    		return Double.class;
    	return valueAt.getClass();
    }
    
    public List<MarketData> getOrders() {
    	return marketDataItems;
    }
}