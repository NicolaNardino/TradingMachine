package com.projects.tradingMachine.utility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.marketData.MarketData;

import quickfix.DataDictionaryProvider;
import quickfix.FixVersions;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.ApplVerID;

public final class Utility {
	private static final Logger logger = LoggerFactory.getLogger(Utility.class);
	private static final Random Random = new Random();
	
	public enum DestinationType {Queue, Topic}
	
	public static void shutdownExecutorService(final ExecutorService es, long timeout, TimeUnit timeUnit) throws InterruptedException {
		es.shutdown();
		if (!es.awaitTermination(timeout, timeUnit))
			es.shutdownNow();
		logger.info("Terminated ScheduledExecutorService");
	}
	
	public static Properties getApplicationProperties(final String propertiesFileName) throws FileNotFoundException, IOException {
		final Properties p = new Properties();
		try(final InputStream inputStream = ClassLoader.getSystemResourceAsStream(propertiesFileName)) {
			p.load(inputStream);
			return p;
		}
	}
	
	public static double roundDouble(final double value, final int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}
	
	public static void sendMessage(final SessionID sessionID, final Message message) {
        try {
            final Session session = Session.lookupSession(sessionID);
            if (session == null) {
                throw new SessionNotFound(sessionID.toString());
            }
            final DataDictionaryProvider dataDictionaryProvider = session.getDataDictionaryProvider();
            try {
                dataDictionaryProvider.getApplicationDataDictionary(getApplVerID(session, message)).validate(message, true);
            } catch (Exception e) {
                LogUtil.logThrowable(sessionID, "Outgoing message failed validation: "+ e.getMessage(), e);
                return;
            }
            session.send(message); //thread safe.
        } catch (final SessionNotFound e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static ApplVerID getApplVerID(final Session session, final Message message) {
        final String beginString = session.getSessionID().getBeginString();
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            return new ApplVerID(ApplVerID.FIX50);
        } else {
            return MessageUtils.toApplVerID(beginString);
        }
    }
    
    public static MarketData buildRandomMarketDataItem(final String symbol) {
    	return new MarketData(symbol, roundDouble(Random.nextDouble() * 100, 2), 
				Utility.roundDouble(Random.nextDouble() * 100, 2), Random.nextInt(1000), Random.nextInt(1000));
    }
}
