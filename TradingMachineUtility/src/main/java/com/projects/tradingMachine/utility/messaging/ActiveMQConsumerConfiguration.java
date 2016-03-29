package com.projects.tradingMachine.utility.messaging;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;

public final class ActiveMQConsumerConfiguration extends ActiveMQConfiguration {
	
	private final MessageConsumer consumer;
	
	public ActiveMQConsumerConfiguration(final Connection connection, final javax.jms.Session session, final MessageConsumer consumer) {
		super(connection, session);
		this.consumer = consumer;
	}
	
	@Override
	public void close() {
		try {
			consumer.close();
		} catch (final JMSException e) {
			throw new RuntimeException(e);
		}
		super.close();
	}

	public final MessageConsumer getConsumer() {
		return consumer;
	}
}
