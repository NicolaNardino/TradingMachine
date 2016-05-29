package com.projects.tradingMachine.tradeMonitor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.services.simulation.orders.RandomOrdersBuilder;
import com.projects.tradingMachine.utility.TradingMachineMessageConsumer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.marketData.MarketData;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * Creates and shows the application, which displays filled and rejected orders. 
 * Initially, the respective JTables are filled with orders coming a MongoDB repository. Then, they get live updates from the executedOrdersTopic.
 * Upon application shutdown, it closes MongoDB and topic subscriber connections.
 * */
public final class TradeMonitorUI implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(TradeMonitorUI.class);
	private final TradingMachineMessageConsumer executedOrdersConsumer;
	private final TradingMachineMessageConsumer marketDataConsumer;
	private final MongoDBManager mongoDBManager;
	private final Comparator<? super SimpleOrder> dateComparator = (c1, c2) -> c1.getStoreDate().compareTo(c2.getStoreDate());
	private final List<SimpleOrder> filledOrders;
	private final List<SimpleOrder> rejectedOrders;
	private final List<MarketData> marketDataItems;
	private final OrdersPanel ordersPanel;
	private final MarketDataPanel marketDataPanel;
	private static final boolean isWithoutLiveFeed = true;
	
	public TradeMonitorUI(final Properties p) throws JMSException, FileNotFoundException, IOException {
		mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.executedOrdersCollection"), p.getProperty("mongoDB.marketDataCollection"));
		executedOrdersConsumer = new TradingMachineMessageConsumer(p.getProperty("activeMQ.url"), 
				p.getProperty("activeMQ.executedOrdersTopic"), DestinationType.Topic, this, "TradeMonitorExecutedOrdersConsumer", null, null);
		marketDataConsumer = new TradingMachineMessageConsumer(p.getProperty("activeMQ.url"), 
				p.getProperty("activeMQ.marketDataTopic"), DestinationType.Topic, this, "TradeMonitorMarketDataConsumer", null, null);
		marketDataItems = mongoDBManager.getMarketData(Optional.ofNullable(null));
		final List<SimpleOrder> backEndOrders = mongoDBManager.getOrders(Optional.ofNullable(null));
		filledOrders = backEndOrders.stream().filter(o -> !o.isRejected()).sorted(dateComparator.reversed()).collect(Collectors.toList());//sorting isn't strictly needed because it'd have been done by the table sorter.
		rejectedOrders = backEndOrders.stream().filter(o -> o.isRejected()).sorted(dateComparator.reversed()).collect(Collectors.toList());
		ordersPanel = new OrdersPanel(filledOrders, rejectedOrders);
		//ordersPanel.getFilledOrdersTable().getColumnModel().getColumn(10).setCellRenderer(new TooltipCellRenderer(marketDataItems));
		marketDataPanel = new MarketDataPanel(marketDataItems);
		executedOrdersConsumer.start();
		marketDataConsumer.start();
		if (isWithoutLiveFeed)
			simulationWithoutLiveFeed();
	}
	
	public void show() throws JMSException, FileNotFoundException, IOException {
		final JFrame frame = new JFrame("Trade Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
        final JTabbedPane tb = new JTabbedPane();
        tb.addTab("Orders", ordersPanel);
        tb.addTab("Market Data", marketDataPanel);
        tb.setSelectedIndex(0);
        tb.setOpaque(true);
        frame.setContentPane(tb);
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	try {
					mongoDBManager.close();
				} catch (final Exception e1) {
					logger.warn("Unable to close the MongoDB connection.\n"+e1.getMessage());
				}
            	try {
            		executedOrdersConsumer.stop();
				} catch (final Exception e1) {
					logger.warn("Unable to close the executedOrdersConsumer topic subscriber.\n"+e1.getMessage());
				}
            	try {
            		marketDataConsumer.stop();
				} catch (final Exception e1) {
					logger.warn("Unable to close the marketDataConsumer topic subscriber.\n"+e1.getMessage());
				}
            	try {
            		ordersPanel.cleanUp();
            		marketDataPanel.cleanUp();
				} catch (final Exception e1) {
					logger.warn("Unable to clean up OrdersPanel resources.\n"+e1.getMessage());
				}
            }
        }); 
        frame.setLocation(new Point(300, 300));
        frame.setSize(new Dimension(1500, 700));
        frame.setVisible(true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage(final Message message) {
		try {
			final Serializable objectMessage = ((ObjectMessage)message).getObject();
			if (objectMessage instanceof SimpleOrder) {
				//gets the filled order and updates and notifies the table model accordingly.
				final SimpleOrder order = (SimpleOrder)objectMessage;
				if (order.isRejected()) { 
					rejectedOrders.add(order);
					((AbstractTableModel)ordersPanel.getRejectedOrdersTable().getModel()).fireTableDataChanged();
				}
				else {
					filledOrders.add(order);
					((AbstractTableModel)ordersPanel.getFilledOrdersTable().getModel()).fireTableDataChanged();	
				}	
			}
			else if (objectMessage instanceof ArrayList<?>) {
				marketDataItems.addAll((ArrayList<MarketData>)objectMessage);
				((AbstractTableModel)marketDataPanel.getMarketDataTable().getModel()).fireTableDataChanged();
			}
			
		} catch (final JMSException e) {
			logger.warn("Failed to process object message, due to "+e.getMessage());
		}
	}

	public static void main(final String[] args) {        
    	javax.swing.SwingUtilities.invokeLater(() -> {
    		try {
				 new TradeMonitorUI(Utility.getApplicationProperties("tradeMonitor.properties")).show();
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}    	
    		});    
    }
    
    private void simulationWithoutLiveFeed() {
    	Executors.newSingleThreadScheduledExecutor().execute(() -> {
    		IntStream.range(1, 100000).forEach(i -> 
    		{
    			Arrays.asList("RIEN", "UBSN", "CSGN").stream().forEach(a -> {
    				marketDataItems.add(Utility.buildRandomMarketDataItem(a));
    				((AbstractTableModel)marketDataPanel.getMarketDataTable().getModel()).fireTableDataChanged();
    				});
    			try {
    				Thread.sleep(500);
    			} catch (final Exception e) {
    				e.printStackTrace();
    			}
    		});
    	});
    	Executors.newSingleThreadScheduledExecutor().execute(() -> {
    		IntStream.range(0, 10000).forEach(i -> {
    			final SimpleOrder randomOrder = RandomOrdersBuilder.build(Arrays.asList("RIEN", "UBSN", "CSGN"));
    			if (randomOrder.isRejected()) {
    				rejectedOrders.add(randomOrder);
        			((AbstractTableModel)ordersPanel.getRejectedOrdersTable().getModel()).fireTableDataChanged();
    			}
    			else {
    				filledOrders.add(randomOrder);
        			((AbstractTableModel)ordersPanel.getFilledOrdersTable().getModel()).fireTableDataChanged();
    			}
    			try {
    				Thread.sleep(500);
    			} catch (final Exception e) {
    				e.printStackTrace();
    			}
    		});
    	});
    }
}
