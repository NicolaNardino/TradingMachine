package com.projects.tradingMachine.tradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.projects.tradingMachine.tradeMonitor.util.GenericListTableModel;
import com.projects.tradingMachine.utility.marketData.MarketData;

/**
 * Custom table model with the initial data set coming from the market data stored in a MongoDB collection, then incremented with updates coming from the topic 
 * MarketDataTopic. 
 * */
public final class MarketDataTableModel extends GenericListTableModel<MarketData>  {
	private static final long serialVersionUID = 1L;
    
    public MarketDataTableModel(final List<MarketData> marketDataItems) throws FileNotFoundException, IOException, JMSException {
    	super(marketDataItems, new String[] {"ID", "Symbol",  "Bid", "Ask", "BidSize", "AskSize", "QuoteTime"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
    	if (data.size() == 0)
    		return null;
        final MarketData marketData = data.get(rowId);
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
}