package com.projects.tradingMachine.TradeMonitor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.table.AbstractTableModel;

import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class TradeMonitorTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] columnNames = {"ID", "Symbol", "Quantity", "Side", "Type", "Time in Force", "Fill Price", "Limit Price", "Stop Price", "Fill Date"};
    
    private final List<SimpleOrder> data;
    
    public TradeMonitorTableModel(final List<SimpleOrder> initialOrders) throws FileNotFoundException, IOException, JMSException {
    	super();
    	data = initialOrders;
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public String getColumnName(final int colId) {
        return columnNames[colId];
    }

    @Override
    public Object getValueAt(final int rowId, final int colId) {
    	Object value = null;
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
                value = order.getAvgPx();
                break;
            case 7:
            	value = order.getLimit();
                break;
            case 8:
                value = order.getStop();
                break;
            case 9:
                value = order.getFillDate();
                break;
        }

        return value;
    }

    @Override
    public Class<?> getColumnClass(final int c) {
    	final Object valueAt = getValueAt(0, c);
    	if (valueAt == null && (c == 7 || c == 8))
    		return Double.class;
		return valueAt.getClass();
    }
}