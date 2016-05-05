package com.projects.tradingMachine.services.database;

import java.sql.SQLException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.services.database.sql.MySqlConnection;
import com.projects.tradingMachine.services.database.sql.MySqlManager;
import com.projects.tradingMachine.utility.ServiceLifeCycle;
import com.projects.tradingMachine.utility.TradingMachineMessageConsumer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class FilledOrdersBackEndStore implements MessageListener, ServiceLifeCycle
{
	private static Logger logger = LoggerFactory.getLogger(FilledOrdersBackEndStore.class);
	
	private final TradingMachineMessageConsumer filledOrdersConsumer;
	private final DataManager mongoDBManager;
	private final DataManager mySqlManager; 
	
	public FilledOrdersBackEndStore(final Properties p) throws JMSException, ClassNotFoundException, SQLException {
		filledOrdersConsumer = new TradingMachineMessageConsumer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.filledOrdersTopic"), DestinationType.Topic, this, "BackEnd", null);
		mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.collection"));
		mySqlManager = new MySqlManager(new MySqlConnection(new DatabaseProperties(p.getProperty("mySQL.host"), Integer.valueOf(p.getProperty("mySQL.port")), p.getProperty("mySQL.database"), 
				p.getProperty("mySQL.userName"), p.getProperty("mySQL.password"))));
	}
	
	@Override
	public void start() throws JMSException {
		filledOrdersConsumer.start();
	}
	
	@Override
	public void stop() throws Exception {
		filledOrdersConsumer.stop();
		mongoDBManager.close();
		mySqlManager.close();
	}
	
	@Override
	public void onMessage(final Message message) {
		try {
			final SimpleOrder order = (SimpleOrder)((ObjectMessage)message).getObject();
			mongoDBManager.storeOrder(order);
			mySqlManager.storeOrder(order);
			
		} catch (final JMSException e) {
			logger.warn("Failed to persist order, due to "+e.getMessage());
		}
	}
	
	public static void main(final String[] args) throws Exception {
		FilledOrdersBackEndStore f = new FilledOrdersBackEndStore(Utility.getApplicationProperties("tradingMachineServices.properties"));
		f.start();
		Thread.sleep(10000);
		f.stop();
	}
}