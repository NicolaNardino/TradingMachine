package com.projects.tradingMachine.services.database;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.noSql.MongoDBConnection;
import com.projects.tradingMachine.services.database.noSql.MongoDBManager;
import com.projects.tradingMachine.services.database.sql.MySqlManager;
import com.projects.tradingMachine.utility.ServiceLifeCycle;
import com.projects.tradingMachine.utility.TradingMachineMessageConsumer;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.database.DatabaseProperties;
import com.projects.tradingMachine.utility.database.MySqlConnection;
import com.projects.tradingMachine.utility.database.creditCheck.CreditCheck;
import com.projects.tradingMachine.utility.database.creditCheck.ICreditCheck;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public final class OrdersBackEndStore implements MessageListener, ServiceLifeCycle
{
	private static Logger logger = LoggerFactory.getLogger(OrdersBackEndStore.class);
	private static final Random randomGenerator = new Random();
	
	private final TradingMachineMessageConsumer ordersConsumer;
	private final DataManager mongoDBManager;
	private final DataManager mySqlManager;
	private final ICreditCheck creditCheck;
	
	public OrdersBackEndStore(final Properties p) throws JMSException, ClassNotFoundException, SQLException {
		ordersConsumer = new TradingMachineMessageConsumer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.executedOrdersTopic"), DestinationType.Topic, this, "BackEnd", null, null);
		mongoDBManager = new MongoDBManager(new MongoDBConnection(new DatabaseProperties(p.getProperty("mongoDB.host"), 
				Integer.valueOf(p.getProperty("mongoDB.port")), p.getProperty("mongoDB.database"))), p.getProperty("mongoDB.executedOrdersCollection"));
		final MySqlConnection mySqlConnection = new MySqlConnection(new DatabaseProperties(p.getProperty("mySQL.host"), 
				Integer.valueOf(p.getProperty("mySQL.port")), p.getProperty("mySQL.database"), 
				p.getProperty("mySQL.userName"), p.getProperty("mySQL.password")));
		mySqlManager = new MySqlManager(mySqlConnection);
		creditCheck = new CreditCheck(mySqlConnection.getConnection());
	}
	
	@Override
	public void start() throws JMSException {
		ordersConsumer.start();
	}
	
	@Override
	public void stop() throws Exception {
		ordersConsumer.stop();
		mongoDBManager.close();
		mySqlManager.close();
	}
	
	@Override
	public void onMessage(final Message message) {
		try {
			final SimpleOrder order = (SimpleOrder)((ObjectMessage)message).getObject();
			if (order.isCreditCheckFailed()) //resets the credit.
				creditCheck.setCredit(Utility.roundDouble(randomGenerator.nextDouble() * 99999, 2));
			mongoDBManager.storeOrder(order);
			mySqlManager.storeOrder(order);
			
		} catch (final JMSException e) {
			logger.warn("Failed to persist order, due to "+e.getMessage());
		}
	}
	
	public static void main(final String[] args) throws Exception {
		OrdersBackEndStore f = new OrdersBackEndStore(Utility.getApplicationProperties("tradingMachineServices.properties"));
		f.start();
		Thread.sleep(10000);
		f.stop();
	}
}