package com.projects.tradingMachine.tradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.projects.tradingMachine.tradeMonitor.util.GenericListTableModel;
import com.projects.tradingMachine.tradeMonitor.util.MarketDataSummary;

public final class MarketDataSummaryTableModel extends GenericListTableModel<MarketDataSummary>  {
	private static final long serialVersionUID = 1L;
    
    public MarketDataSummaryTableModel(final List<MarketDataSummary> marketDataSummaryItems) throws FileNotFoundException, IOException, JMSException {
    	super(marketDataSummaryItems, new String[] {"Symbol",  "Avg. Bid", "Avg. Ask", "Avg. BidSize", "Avg. AskSize", "Nr. of Items"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
    	if (data.size() == 0)
    		return null;
        final MarketDataSummary marketDataSummary = data.get(rowId);
        switch (colId) {
            case 0:
                value = marketDataSummary.getSymbol();
                break;
            case 1:
                value = marketDataSummary.getAvgBid();
                break;
            case 2:
                value = marketDataSummary.getAvgAsk();
                break;
            case 3:
                value = marketDataSummary.getAvgBidSize();
                break;
            case 4:
                value = marketDataSummary.getAvgAskSize();
                break;
            case 5:
                value = marketDataSummary.getItemsNumber();
                break;
        }
        return value;
    }
}