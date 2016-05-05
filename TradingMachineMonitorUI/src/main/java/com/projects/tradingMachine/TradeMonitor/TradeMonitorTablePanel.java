package com.projects.tradingMachine.TradeMonitor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class TradeMonitorTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JTable ordersTable;
    
	public TradeMonitorTablePanel(final List<SimpleOrder> orders) throws FileNotFoundException, IOException, JMSException {
        super(new GridLayout(1,0)); 
        ordersTable = new JTable(new TradeMonitorTableModel(orders));      
        ordersTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected, final boolean hasFocus,
                    final int row, final int column) {
                value = f.format(value);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        final TableRowSorter<TableModel> tableSorter = new TableRowSorter<TableModel>(ordersTable.getModel());
		ordersTable.setRowSorter(tableSorter);
        final List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(9, SortOrder.DESCENDING));
        tableSorter.setSortKeys(sortKeys);
        tableSorter.sort();
        ordersTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersTable.setFillsViewportHeight(true);
        add(new JScrollPane(ordersTable));
    }
	
	public JTable getOrdersTable() {
		return ordersTable;
	}
}
