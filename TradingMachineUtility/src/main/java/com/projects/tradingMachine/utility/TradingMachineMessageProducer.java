package com.projects.tradingMachine.utility;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.projects.tradingMachine.utility.Utility.DestinationType;

public final class TradingMachineMessageProducer implements ServiceLifeCycle {

	private final Connection connection;
	private final Session session;
	private final MessageProducer producer;
	
	public TradingMachineMessageProducer(final String brokerUrl, final String destinationName, final DestinationType destinationType, 
			final ExceptionListener exceptionListener) throws JMSException {
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection();
        connection.setClientID(destinationName + "Producer");
        session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(destinationType == DestinationType.Queue ?  session.createQueue(destinationName) : session.createTopic(destinationName));
        if (exceptionListener != null)
        	connection.setExceptionListener(exceptionListener);
	}
	
	
	public Session getSession() {
		return session;
	}


	public MessageProducer getProducer() {
		return producer;
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
