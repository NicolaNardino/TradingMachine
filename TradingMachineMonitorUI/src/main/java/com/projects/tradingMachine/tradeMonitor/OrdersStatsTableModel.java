package com.projects.tradingMachine.tradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.projects.tradingMachine.tradeMonitor.util.GenericListTableModel;
import com.projects.tradingMachine.tradeMonitor.util.OrdersStats;

public final class OrdersStatsTableModel extends GenericListTableModel<OrdersStats> {
	private static final long serialVersionUID = 1L;
        
    public OrdersStatsTableModel(final List<OrdersStats> orderStats) throws FileNotFoundException, IOException, JMSException {
    	super(orderStats, new String[] {"Total", "Filled", "Rejected", "Buy", "Sell", "Market", "Limit", "Stop"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
    	if (data.size() == 0)
    		return null;
        final OrdersStats ordersStats = data.get(rowId);
        switch (colId) {
            case 0:
                value = ordersStats.getTotal();
                break;
            case 1:
                value = ordersStats.getFilled();
                break;
            case 2:
                value = ordersStats.getRejected();
                break;
            case 3:
                value = ordersStats.getBuy();
                break;
            case 4:
                value = ordersStats.getSell();
                break;
            case 5:
                value = ordersStats.getMarket();
                break;
            case 6:
                value = ordersStats.getLimit();
                break;
            case 7:
            	value = ordersStats.getStop();
                break;
        }

        return value;
    }
}