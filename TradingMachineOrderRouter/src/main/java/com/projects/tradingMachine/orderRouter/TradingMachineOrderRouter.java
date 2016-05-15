package com.projects.tradingMachine.orderRouter;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;

/**
 * IX initiator.
 * */
public class TradingMachineOrderRouter {
	private static final Logger logger = LoggerFactory.getLogger(TradingMachineOrderRouter.class);
	private static final CountDownLatch monitorLatch = new CountDownLatch(1);
	private final TradingMachineFixInitiatorApplication myApplication;

	private final Initiator initiator;

	public TradingMachineOrderRouter() throws Exception {
		final SessionSettings settings = getSessionSettings();
		final MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
		final LogFactory logFactory = new ScreenLogFactory(true, true, true, true);
		final MessageFactory messageFactory = new DefaultMessageFactory();
		myApplication = new TradingMachineFixInitiatorApplication(settings);
		initiator = new SocketInitiator(myApplication, messageStoreFactory, settings, logFactory, messageFactory);
		new JmxExporter().register(initiator);
	}

	private SessionSettings getSessionSettings() throws IOException, ConfigError {
		try (final InputStream inputStream = TradingMachineOrderRouter.class.getResourceAsStream("/tradingMachineOrderRouterFixEngine.properties");) {
			return new SessionSettings(inputStream);
		}
	}

	public TradingMachineFixInitiatorApplication getMyApplication() {
		return myApplication;
	}


	public void start() throws RuntimeError, ConfigError {
		initiator.start();
		logger.info("Initiator started.");
	}

	public void stop() throws Exception {
		initiator.stop();
		logger.info("Initiator stopped.");
		myApplication.closeOrdersConsumer();
		monitorLatch.countDown();
	}

	public void logon() {
		initiator.getSessions().stream().forEach(sessionId -> Session.lookupSession(sessionId).logon());
	}

	public static void main(final String[] args) throws Exception {
		final TradingMachineOrderRouter initiator = new TradingMachineOrderRouter();
		initiator.start();
		initiator.logon();
		monitorLatch.await();
	}
}