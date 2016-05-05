package com.projects.tradingMachine.TradeMonitor;

import java.awt.Color;
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
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * It creates a panel with a table as the only content and:  
 * 	<ul>
 * 		<li>Sets a column renderer on the FilledDate column.</li>
 * 		<li>Overrides JTable.prepareRenderer in order to show BUY and SELL order rows with different colors.</li>
 * 		<li>Sets a TableRowSorter with default sorting enabled on the FilledDate column.</li>
 * 	</ul>
 * */
public final class TradeMonitorTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JTable ordersTable;
    
	public TradeMonitorTablePanel(final List<SimpleOrder> orders) throws FileNotFoundException, IOException, JMSException {
        super(new GridLayout(1,0)); 
        final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        ordersTable = new JTable(new TradeMonitorTableModel(orders)) {
			private static final long serialVersionUID = 1L;
        	public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column)
        	    {
        	        final Component component = super.prepareRenderer(renderer, row, column);
	                switch ((OrderSide)getModel().getValueAt(convertRowIndexToModel(row), 3)) {
					case BUY:
						component.setBackground(Color.BLACK);
						component.setForeground(Color.WHITE);
						break;
					case SELL:
						component.setBackground(Color.RED);
						component.setForeground(Color.YELLOW);
	                    break;
					default:
						break;
					}
        	        return component;
        	    }
        };
        ordersTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected, final boolean hasFocus,
                    final int row, final int column) {
                value = f.format(value);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        //pre-set sorter enabled on the FilledDate column.
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
