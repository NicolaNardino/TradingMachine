package com.projects.tradingMachine.server;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.database.DatabaseProperties;
import com.projects.tradingMachine.utility.database.PooledDataSourceBuilder;

import quickfix.ConfigError;
import quickfix.Dictionary;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.fixt11.Logon;

/**
 * QuickFIX/J acceptor with following key features:
 * <ul>
 * 	<li>Does log-on check.</li>
 *  <li>FIX 5.0 message cracking. Once a message is received, it then gets forwarded, in a separate thread, to a matching engine.</li>
 *  <li>The degree of parallelism can be set by a configuration parameter.</li> 
 *  <li>Receives market data from a given queue.</li>
 * </ul>
 * */
public class TradingMachineFixAcceptorApplication extends quickfix.MessageCracker implements quickfix.Application {
	private final static Logger logger = LoggerFactory.getLogger(TradingMachineFixAcceptorApplication.class);
	
    private final MarketDataManager marketDataManager;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutorService;
    private final SessionSettings settings;
    private final BasicDataSource creditCheckConnectionPool;
    
    public TradingMachineFixAcceptorApplication(final SessionSettings settings) throws Exception {
    	this.settings = settings;
    	final Properties applicationProperties = Utility.getApplicationProperties("tradingMachine.properties");
		marketDataManager = new MarketDataManager(applicationProperties);
		marketDataManager.start();
        executor = Executors.newFixedThreadPool(Integer.valueOf(applicationProperties.getProperty("numberProcessingOrderThreads")));
        creditCheckConnectionPool = PooledDataSourceBuilder.getDataSource(new DatabaseProperties(applicationProperties.getProperty("mySQL.host"), 
        		Integer.valueOf(applicationProperties.getProperty("mySQL.port")), applicationProperties.getProperty("mySQL.database"), 
        		applicationProperties.getProperty("mySQL.userName"), applicationProperties.getProperty("mySQL.password")), 
        		Integer.valueOf(applicationProperties.getProperty("creditCheckDatabasePoolConnections")));
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
			logger.debug("Credit check database pool,  idle: "+creditCheckConnectionPool.getNumIdle()+", active: "+creditCheckConnectionPool.getNumActive());
        }, 1, 10, TimeUnit.SECONDS); 
    }

    @Override
    public void onCreate(final SessionID sessionID) {
        Session.lookupSession(sessionID).getLog().onEvent("Session "+sessionID+" created.");
    }

    @Override
    public void onLogon(final SessionID sessionID) {
    	
    }

    @Override
    public void onLogout(final SessionID sessionID) {
    }

    @Override
    public void toAdmin(quickfix.Message message, SessionID sessionID) {
    }

    @Override
    public void toApp(quickfix.Message message, SessionID sessionID) throws DoNotSend {
    }

    @Override
    public void fromAdmin(final quickfix.Message message, final SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, RejectLogon {
    	logonCheck(message, sessionID);     
    }
    
    private void logonCheck(final quickfix.Message message, final SessionID sessionID) throws RejectLogon {
    	if (message instanceof Logon) {
    		final Logon logon = (Logon)message;
    		try {
    			final Dictionary sessionSettings = settings.get(sessionID);
        		final String userName = logon.getUsername().getValue();
        		final String password = logon.getPassword().getValue();
        		final String configuredUserName = sessionSettings.getString("UserName");
        		final String configuredPassword = sessionSettings.getString("Password");
    			if (!configuredUserName.equals(userName))
        			throw new RejectLogon("Username "+userName+" doesn't match the excepted one: "+configuredUserName);
    			if (!configuredPassword.equals(password))
        			throw new RejectLogon("Password mismatch.");
    		}
    		catch(final Exception ex) {
    			if (ex instanceof RejectLogon)
    				throw new RejectLogon(ex.getMessage());
    			throw new RejectLogon("Unable to check logon credentials, due to: "+ex.getMessage());
    		}
    	}
    }
    
    public void fromApp(quickfix.Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat,
            IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }
    
    public void onMessage(final quickfix.fix50.NewOrderSingle order, final SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue, NumberFormatException, ClassNotFoundException, SQLException, ConfigError, FieldConvertError {
    	executor.execute(new MatchingEngine(creditCheckConnectionPool, marketDataManager, order, sessionID));
    } 
    
    public void cleanUp() {
    	try {
    		Utility.shutdownExecutorService(executor, 5, TimeUnit.SECONDS);
    	}
    	catch(final InterruptedException ex) {
    		logger.warn("Exception while shutting down the matching engine executor service.");
    	}
    	try {
    		Utility.shutdownExecutorService(scheduledExecutorService, 5, TimeUnit.SECONDS);
    	}
    	catch(final InterruptedException ex) {
    		logger.warn("Exception while shutting down utility scheduled executor service.");
    	}
    	try {
			creditCheckConnectionPool.close();
		} catch (final SQLException ex) {
			logger.warn("Exception while closing database connection pool.");
		}
    }
}