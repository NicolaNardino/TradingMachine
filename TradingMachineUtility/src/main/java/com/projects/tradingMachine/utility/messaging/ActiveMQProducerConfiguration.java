package com.projects.tradingMachine.utility.messaging;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

public final class ActiveMQProducerConfiguration extends ActiveMQConfiguration {
	
	private final MessageProducer producer;
	
	public ActiveMQProducerConfiguration(final Connection connection, final javax.jms.Session session, final MessageProducer producer) {
		super(connection, session);
		this.producer = producer;
	}
	
	@Override
	public void close() {
		try {
			producer.close();
		} catch (final JMSException e) {
			throw new RuntimeException(e);
		}
		super.close();
	}
	
	public MessageProducer getProducer() {
		return producer;
	}
}
