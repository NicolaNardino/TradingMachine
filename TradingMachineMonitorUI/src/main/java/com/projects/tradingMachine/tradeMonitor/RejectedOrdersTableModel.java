package com.projects.tradingMachine.tradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;

import com.projects.tradingMachine.tradeMonitor.util.GenericListTableModel;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * Custom table model with the initial data set coming from the orders stored in a MongoDB collection, then incremented with the ones received onto the 
 * ExecutedOrdersTopic. 
 * */
public final class RejectedOrdersTableModel extends GenericListTableModel<SimpleOrder> {
	private static final long serialVersionUID = 1L;
        
    public RejectedOrdersTableModel(final List<SimpleOrder> rejectedOrders) throws FileNotFoundException, IOException, JMSException {
    	super(rejectedOrders, new String[] {"ID", "Symbol", "Quantity", "Side", "Type", "Time in Force", "Limit Price", "Stop Price", "Reject Date", "Credit Check Failed"});
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
    	if (data.size() == 0)
    		return null;
        final SimpleOrder order = data.get(rowId);
        switch (colId) {
            case 0:
                value = order.getID();
                break;
            case 1:
                value = order.getSymbol();
                break;
            case 2:
                value = order.getQuantity();
                break;
            case 3:
                value = order.getSide();
                break;
            case 4:
                value = order.getType();
                break;
            case 5:
                value = order.getTimeInForce();
                break;
            case 6:
            	value = order.getLimit();
                break;
            case 7:
                value = order.getStop();
                break;
            case 8:
                value = order.getStoreDate();
                break;
            case 9:
                value = order.isCreditCheckFailed();
                break;
        }

        return value;
    }
}