package com.projects.tradingMachine.server;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionSettings;
import quickfix.SocketAcceptor;

/**
 * Main class for configuring and starting the FIX acceptor.
 * */
public final class TradingMachineServer {
	private final static Logger logger = LoggerFactory.getLogger(TradingMachineServer.class);
	private final SocketAcceptor acceptor;

	public TradingMachineServer() throws Exception {
		final SessionSettings settings = getSessionSettings();
		final TradingMachineFixAcceptorApplication application = new TradingMachineFixAcceptorApplication(settings);
		final MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
		final LogFactory logFactory = new ScreenLogFactory(true, true, true);
		final MessageFactory messageFactory = new DefaultMessageFactory();
		acceptor = new SocketAcceptor(application, messageStoreFactory, settings, logFactory, messageFactory);
	}

	private SessionSettings getSessionSettings() throws IOException, ConfigError {
		try (final InputStream inputStream = TradingMachineServer.class.getResourceAsStream("/tradingMachineFixEngine.properties");) {
			return new SessionSettings(inputStream);
		}
	}

	private void start() throws RuntimeError, ConfigError {
		acceptor.start();
	}

	private void stop() {
		acceptor.stop();
	}

	public static void main(String[] args) throws Exception {
		try {
			final TradingMachineServer executor = new TradingMachineServer();
			executor.start();
			logger.info("press <enter> to quit");
			System.in.read();
			executor.stop();
		} catch (final Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}