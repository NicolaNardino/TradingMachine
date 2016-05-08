package com.projects.tradingMachine.TradeMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * It creates a panel with a table as main content and:  
 * 	<ul>
 * 		<li>Sets a column renderer on the FilledDate column.</li>
 * 		<li>Overrides JTable.prepareRenderer in order to show BUY and SELL order rows with different colors.</li>
 * 		<li>Sets a TableRowSorter with default sorting enabled on the FilledDate column.</li>
 * 		<li>As a side content, North to the table, it shows statistics about the orders which get updated every 1 second.</li>
 * 	</ul>
 * */
public final class TradeMonitorTablePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JTable ordersTable;
	private final  Map<StatsLabel, JLabel> statLabels = new HashMap<>();
	private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
	
	public TradeMonitorTablePanel(final List<SimpleOrder> orders) throws FileNotFoundException, IOException, JMSException {
        super(new BorderLayout()); 
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
        add(new JScrollPane(ordersTable), BorderLayout.CENTER);
        add(buildStatsPanel(), BorderLayout.NORTH);
        //Orders stats get updated every 1 sec. 
        es.scheduleWithFixedDelay(() -> {
        	javax.swing.SwingUtilities.invokeLater(() -> {
        		final Map<OrderSide, List<SimpleOrder>> ordersBySide = orders.stream().collect(Collectors.groupingBy(SimpleOrder::getSide));
        		final Map<OrderType, List<SimpleOrder>> ordersByType = orders.stream().collect(Collectors.groupingBy(SimpleOrder::getType));
        		statLabels.get(StatsLabel.TOTAL).setText(StatsLabel.TOTAL.toString()+orders.size());
            	statLabels.get(StatsLabel.BUY).setText(StatsLabel.BUY.toString()+ordersBySide.get(OrderSide.BUY).size());
            	statLabels.get(StatsLabel.SELL).setText(StatsLabel.SELL.toString()+ordersBySide.get(OrderSide.SELL).size());
            	statLabels.get(StatsLabel.MARKET).setText(StatsLabel.MARKET.toString()+ordersByType.get(OrderType.MARKET).size());
            	statLabels.get(StatsLabel.LIMIT).setText(StatsLabel.LIMIT.toString()+ordersByType.get(OrderType.LIMIT).size());
            	statLabels.get(StatsLabel.STOP).setText(StatsLabel.STOP.toString()+ordersByType.get(OrderType.STOP).size());    	
        		}); 
        }, 1, 1, TimeUnit.SECONDS); 
    }
	
	private JPanel buildStatsPanel() {
		final List<SimpleOrder> orders = ((TradeMonitorTableModel)ordersTable.getModel()).getOrders();
		final JPanel panel = new JPanel(new GridLayout(2, 3, 10, 5));
		final Map<OrderSide, List<SimpleOrder>> ordersBySide = orders.stream().collect(Collectors.groupingBy(SimpleOrder::getSide));
		final Map<OrderType, List<SimpleOrder>> ordersByType = orders.stream().collect(Collectors.groupingBy(SimpleOrder::getType));
		statLabels.put(StatsLabel.TOTAL, new JLabel(StatsLabel.TOTAL.toString()+orders.size()));
		panel.add(statLabels.get(StatsLabel.TOTAL));
		statLabels.put(StatsLabel.BUY, new JLabel(StatsLabel.BUY.toString()+ordersBySide.get(OrderSide.BUY).size()));
		panel.add(statLabels.get(StatsLabel.BUY));
		statLabels.put(StatsLabel.SELL, new JLabel(StatsLabel.SELL.toString()+ordersBySide.get(OrderSide.SELL).size()));
		panel.add(statLabels.get(StatsLabel.SELL));
		statLabels.put(StatsLabel.MARKET, new JLabel(StatsLabel.MARKET.toString()+ordersByType.get(OrderType.MARKET).size()));
		panel.add(statLabels.get(StatsLabel.MARKET));
		statLabels.put(StatsLabel.LIMIT, new JLabel(StatsLabel.LIMIT.toString()+ordersByType.get(OrderType.LIMIT).size()));
		panel.add(statLabels.get(StatsLabel.LIMIT));
		statLabels.put(StatsLabel.STOP, new JLabel(StatsLabel.STOP.toString()+ordersByType.get(OrderType.STOP).size()));
		panel.add(statLabels.get(StatsLabel.STOP));
		return panel;
	}
	
	public JTable getOrdersTable() {
		return ordersTable;
	}
	
	public void cleanUp() throws InterruptedException {
		Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS);
	}
	
	private enum StatsLabel {
		TOTAL("Total orders: "), BUY("Buy: "), SELL("Sell: "), MARKET("Market: "), LIMIT("Limit: "), STOP("Stop: ");
		private final String description;
		
	    private StatsLabel(final String name) {
	        this.description = name;
	    }

	    @Override
	    public String toString() {
	        return description;
	    }
	}
}
