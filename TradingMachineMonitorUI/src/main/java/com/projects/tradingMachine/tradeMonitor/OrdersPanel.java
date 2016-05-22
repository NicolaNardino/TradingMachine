package com.projects.tradingMachine.tradeMonitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import javax.swing.table.TableCellRenderer;

import com.projects.tradingMachine.tradeMonitor.util.DatetimeTableCellRenderer;
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
public final class OrdersPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final List<SimpleOrder> filledOrders;
	private final List<SimpleOrder> rejectedOrders;
	private JTable filledOrdersTable;
	private JTable rejectedOrdersTable;
	private final  Map<StatsLabel, JLabel> statLabels = new HashMap<>();
	private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
	
	public OrdersPanel(final List<SimpleOrder> filledOrders, final List<SimpleOrder> rejectedOrders) throws FileNotFoundException, IOException, JMSException {
        super(new BorderLayout(10, 20)); 
        this.filledOrders = filledOrders;
        this.rejectedOrders = rejectedOrders;
        filledOrdersTable = buildOrdersTable(filledOrders);
        rejectedOrdersTable = buildOrdersTable(rejectedOrders);
        add(buildStatsPanel(), BorderLayout.NORTH);
        final JScrollPane filledOrdersScrollPane = new JScrollPane(filledOrdersTable);
        filledOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Filled Orders"));
		add(filledOrdersScrollPane, BorderLayout.CENTER);
        final JScrollPane rejectedOrdersScrollPane = new JScrollPane(rejectedOrdersTable);
        rejectedOrdersScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Rejected Orders"));
		add(rejectedOrdersScrollPane, BorderLayout.SOUTH);
        
        //Orders stats get updated every 1 sec. 
        es.scheduleWithFixedDelay(() -> {
        	javax.swing.SwingUtilities.invokeLater(() -> {
        		final Map<OrderSide, List<SimpleOrder>> ordersBySide = filledOrders.stream().collect(Collectors.groupingBy(SimpleOrder::getSide));
        		final Map<OrderType, List<SimpleOrder>> ordersByType = filledOrders.stream().collect(Collectors.groupingBy(SimpleOrder::getType));
        		statLabels.get(StatsLabel.TOTAL).setText(StatsLabel.TOTAL.toString()+(filledOrders.size() + rejectedOrders.size()));
        		statLabels.get(StatsLabel.FILLED).setText(StatsLabel.FILLED.toString()+filledOrders.size());
        		statLabels.get(StatsLabel.REJECTED).setText(StatsLabel.REJECTED.toString()+rejectedOrders.size());
        		statLabels.get(StatsLabel.TOTAL).setText(StatsLabel.TOTAL.toString()+(filledOrders.size() + rejectedOrders.size()));
            	statLabels.get(StatsLabel.BUY).setText(StatsLabel.BUY.toString()+ordersBySide.get(OrderSide.BUY).size());
            	statLabels.get(StatsLabel.SELL).setText(StatsLabel.SELL.toString()+ordersBySide.get(OrderSide.SELL).size());
            	statLabels.get(StatsLabel.MARKET).setText(StatsLabel.MARKET.toString()+ordersByType.get(OrderType.MARKET).size());
            	statLabels.get(StatsLabel.LIMIT).setText(StatsLabel.LIMIT.toString()+ordersByType.get(OrderType.LIMIT).size());
            	statLabels.get(StatsLabel.STOP).setText(StatsLabel.STOP.toString()+ordersByType.get(OrderType.STOP).size());    	
        		});
        }, 1, 1, TimeUnit.SECONDS); 
    }
	
	private static JTable buildOrdersTable(final List<SimpleOrder> orders) throws FileNotFoundException, IOException, JMSException {
		final JTable ordersTable = new JTable(new OrdersTableModel(orders)) {
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
        ordersTable.getColumnModel().getColumn(9).setCellRenderer(new DatetimeTableCellRenderer(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
        //pre-set sorter enabled on the FilledDate column.
        SwingUtility.setTableSorter(ordersTable, 9, SortOrder.DESCENDING);
        ordersTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        ordersTable.setFillsViewportHeight(true);
        return ordersTable;
	}
	
	
	
	private JPanel buildStatsPanel() {
		final JPanel panel = new JPanel(new GridLayout(1, 8, 10, 5));
		final Map<OrderSide, List<SimpleOrder>> ordersBySide = filledOrders.stream().collect(Collectors.groupingBy(SimpleOrder::getSide));
		final Map<OrderType, List<SimpleOrder>> ordersByType = filledOrders.stream().collect(Collectors.groupingBy(SimpleOrder::getType));
		statLabels.put(StatsLabel.TOTAL, new JLabel(StatsLabel.TOTAL.toString()+ (filledOrders.size() + rejectedOrders.size())));
		panel.add(statLabels.get(StatsLabel.TOTAL));
		statLabels.put(StatsLabel.FILLED, new JLabel(StatsLabel.FILLED.toString()+filledOrders.size()));
		panel.add(statLabels.get(StatsLabel.FILLED));
		statLabels.put(StatsLabel.REJECTED, new JLabel(StatsLabel.REJECTED.toString()+rejectedOrders.size()));
		panel.add(statLabels.get(StatsLabel.REJECTED));
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
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Statistics"));
		return panel;
	}
	
	public JTable getFilledOrdersTable() {
		return filledOrdersTable;
	}
	
	public JTable getRejectedOrdersTable() {
		return rejectedOrdersTable;
	}
	
	public void cleanUp() throws InterruptedException {
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
