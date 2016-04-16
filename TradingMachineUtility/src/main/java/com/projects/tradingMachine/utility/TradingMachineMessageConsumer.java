package com.projects.tradingMachine.utility;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.projects.tradingMachine.utility.Utility.DestinationType;

public final class TradingMachineMessageConsumer implements ServiceLifeCycle {

	private final Connection connection;
	private final Session session;
	private final MessageConsumer consumer;
	
	public TradingMachineMessageConsumer(final String brokerUrl, final String destinationName, final DestinationType destinationType, 
			final MessageListener messageListener, final ExceptionListener exceptionListener) throws JMSException {
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection();
        connection.setClientID(destinationName + "Consumer");
        session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(destinationType == DestinationType.Queue ?  session.createQueue(destinationName) : session.createTopic(destinationName));
        if (messageListener != null)
        	consumer.setMessageListener(messageListener);
        if (exceptionListener != null)
        	connection.setExceptionListener(exceptionListener);
	}
	
	@Override
	public void start() throws JMSException {
		connection.start();
	}

	@Override
	public void stop() throws JMSException {
		connection.close();
	}
}
