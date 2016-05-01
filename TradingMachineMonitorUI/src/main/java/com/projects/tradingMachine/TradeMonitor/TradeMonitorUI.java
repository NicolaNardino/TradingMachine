package com.projects.tradingMachine.TradeMonitor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public class TradeMonitorUI {
	
	private static Logger logger = LoggerFactory.getLogger(TradeMonitorUI.class);
	
    private static void createAndShowGUI(final Properties p) {
    	final MongoDBManager mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.collection"));
        final JFrame frame = new JFrame("Trade Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
		final Comparator<? super SimpleOrder> dateComparator = (c1, c2) -> c1.getFillDate().compareTo(c2.getFillDate());
		TradeMonitorTablePanel newContentPane = new TradeMonitorTablePanel(mongoDBManager.getOrders(Optional.ofNullable(null)).stream().
				sorted(dateComparator.reversed()).collect(Collectors.toList()));
        newContentPane.setOpaque(true); 
        frame.setContentPane(newContentPane);
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
            	try {
					mongoDBManager.close();
				} catch (final Exception e1) {
					logger.warn("Unable to close the MongoDB connection.\n"+e1.getMessage());
				}
            }
        }); 
        frame.setLocation(new Point(300, 300));
        frame.setSize(new Dimension(1500, 500));
        frame.setVisible(true);
    }
 
    public static void main(final String[] args) {        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
					createAndShowGUI(Utility.getApplicationProperties("tradeMonitor.properties"));
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
            }
        });
    }
}
