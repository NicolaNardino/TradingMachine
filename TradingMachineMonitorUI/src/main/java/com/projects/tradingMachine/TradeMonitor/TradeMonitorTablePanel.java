package com.projects.tradingMachine.TradeMonitor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.projects.tradingMachine.utility.order.SimpleOrder;

public class TradeMonitorTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
 
    public TradeMonitorTablePanel(final List<SimpleOrder> orders) {
        super(new GridLayout(1,0)); 
        final JTable table = new JTable(new TradeMonitorTableModel(orders));      
        table.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected, final boolean hasFocus,
                    final int row, final int column) {
                value = f.format(value);
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        
        add(new JScrollPane(table));
    }
}
