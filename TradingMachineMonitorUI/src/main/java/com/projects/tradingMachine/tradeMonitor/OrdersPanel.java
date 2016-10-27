package com.projects.tradingMachine.tradeMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import com.projects.tradingMachine.tradeMonitor.util.DatetimeTableCellRenderer;
import com.projects.tradingMachine.tradeMonitor.util.OrdersStats;
import com.projects.tradingMachine.tradeMonitor.util.PanelCleanUp;
import com.projects.tradingMachine.tradeMonitor.util.SwingUtility;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * It creates a panel with a executed and rejected orders tables and:  
 * 	<ul>
 * 		<li>Sets a column renderer on the FilledDate column.</li>
 * 		<li>Overrides JTable.prepareRenderer in order to show BUY and SELL order rows with different colors.</li>
 * 		<li>Sets a TableRowSorter with default sorting enabled on the FilledDate column.</li>
 * 		<li>As a side content, North to the tables, it shows statistics about the orders, which get updated every 1 second.</li>
 * 	</ul>
 * */
public final class OrdersPanel extends JPanel implements PanelCleanUp {
	private static final long serialVersionUID = 1L;
	private final List<SimpleOrder> filledOrders, rejectedOrders;
	private final List<OrdersStats> ordersStats;
	private JTable filledOrdersTable, rejectedOrdersTable, ordersStatsTable;
	private final  Map<StatsLabel, JLabel> statLabels = new HashMap<>();
	private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
	
	private static OrdersStats buildOrdersStats(final List<SimpleOrder> allOrders) {
		final Map<Boolean, Long> groupedByRjectionStatus = allOrders.parallelStream().
				collect(Collectors.partitioningBy(SimpleOrder::isRejected, Collectors.counting()));
		final Map<OrderSide, Long> groupedBySide = allOrders.parallelStream().
				collect(Collectors.groupingBy(SimpleOrder::getSide, Collectors.counting()));
		final Map<OrderType, Long> groupedByType = allOrders.parallelStream().
				collect(Collectors.groupingBy(SimpleOrder::getType, Collectors.counting()));
		return new OrdersStats(allOrders.size(), groupedByRjectionStatus.get(false), groupedByRjectionStatus.get(true), 
				groupedBySide.get(OrderSide.BUY), groupedBySide.get(OrderSide.SELL), groupedByType.get(OrderType.MARKET), 
				groupedByType.get(OrderType.LIMIT), groupedByType.get(OrderType.STOP));
	}
	
	public OrdersPanel(final List<SimpleOrder> filledOrders, final List<SimpleOrder> rejectedOrders) throws FileNotFoundException, IOException, JMSException {
        super(new BorderLayout(10, 20)); 
        this.filledOrders = filledOrders;
        this.rejectedOrders = rejectedOrders;
        filledOrdersTable = buildOrdersTable(filledOrders, true);
        rejectedOrdersTable = buildOrdersTable(rejectedOrders, false);
        ordersStats = Arrays.asList(buildOrdersStats(java.util.stream.Stream.concat(filledOrders.stream(), rejectedOrders.stream()).collect(Collectors.toList())));
        ordersStatsTable = new JTable(new OrdersStatsTableModel(ordersStats));
        ordersStatsTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersStatsTable.setFillsViewportHeight(true);
        add(ordersStatsTable, BorderLayout.NORTH);
        final JScrollPane filledOrdersScrollPane = new JScrollPane(filledOrdersTable);
        filledOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Filled Orders"));
		add(filledOrdersScrollPane, BorderLayout.CENTER);
        final JScrollPane rejectedOrdersScrollPane = new JScrollPane(rejectedOrdersTable);
        rejectedOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Rejected Orders"));
		add(rejectedOrdersScrollPane, BorderLayout.SOUTH);
		//Orders stats get updated every 1 sec. 
		es.scheduleWithFixedDelay(() -> {
			ordersStats.clear();
			ordersStats.add(buildOrdersStats(java.util.stream.Stream.concat(filledOrders.stream(), rejectedOrders.stream()).collect(Collectors.toList())));
			((AbstractTableModel)ordersStatsTable.getModel()).fireTableDataChanged();
        }, 1, 1, TimeUnit.SECONDS); 
    }
	
	private static JTable buildOrdersTable(final List<SimpleOrder> orders, boolean filled) throws FileNotFoundException, IOException, JMSException {
		final JTable ordersTable = new JTable(filled ? new FilledOrdersTableModel(orders) : new RejectedOrdersTableModel(orders)) {
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
        ordersTable.getColumnModel().getColumn(filled ? 9 : 8).setCellRenderer(new DatetimeTableCellRenderer(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
        //pre-set sorter enabled on the FilledDate column.
        SwingUtility.setTableSorter(ordersTable, filled ? 9 : 8, SortOrder.DESCENDING);
        ordersTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersTable.setFillsViewportHeight(true);
        return ordersTable;
	}
	
	public JTable getFilledOrdersTable() {
		return filledOrdersTable;
	}
	
	public JTable getRejectedOrdersTable() {
		return rejectedOrdersTable;
	}
	
	@Override
	public void cleanUp() throws Exception {
		Utility.shutdownExecutorService(es, 5, TimeUnit.SECONDS);
	}
	
	private enum StatsLabel {
		TOTAL("Total orders: "), BUY("Buy: "), SELL("Sell: "), MARKET("Market: "), LIMIT("Limit: "), STOP("Stop: "), FILLED("Filled: "), REJECTED("Rejected: ");
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
