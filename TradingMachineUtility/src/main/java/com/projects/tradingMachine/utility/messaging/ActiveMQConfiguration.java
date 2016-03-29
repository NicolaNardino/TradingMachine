package com.projects.tradingMachine.utility.messaging;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveMQConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(ActiveMQConfiguration.class);
	
	private final Connection connection;
	private final javax.jms.Session session;
	
	public ActiveMQConfiguration(final Connection connection, final javax.jms.Session session) {
		this.connection = connection;
		this.session = session;
	}
	
	public void close() {
		try {
			session.close();
            connection.close();
		} catch (final JMSException e) {
			logger.warn(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public final javax.jms.Session getSession() {
		return session;
	}

	public final Connection getConnection() {
		return connection;
	}
	
}