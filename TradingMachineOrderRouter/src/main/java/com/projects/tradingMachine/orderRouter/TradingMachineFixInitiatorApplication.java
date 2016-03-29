package com.projects.tradingMachine.orderRouter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.Utility.DestinationType;
import com.projects.tradingMachine.utility.messaging.ActiveMQConsumerConfiguration;
import com.projects.tradingMachine.utility.messaging.ActiveMQProducerConfiguration;
import com.projects.tradingMachine.utility.order.SimpleOrder;

import quickfix.Application;
import quickfix.Dictionary;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.HandlInst;
import quickfix.field.LeavesQty;
import quickfix.field.MsgType;
import quickfix.field.OrdStatus;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TransactTime;

public class TradingMachineFixInitiatorApplication implements Application, MessageListener, ExceptionListener {
	private static final Logger logger = LoggerFactory.getLogger(TradingMachineFixInitiatorApplication.class);
	
	private final SessionSettings settings;
	private final OrderManager orderManager;
	private final ActiveMQConsumerConfiguration activeMQOrdersConsumerConfiguration;
	private final ActiveMQProducerConfiguration activeMQOrdersProducerConfiguration;
	private final Set<SessionID> loggedOnSessions;
	
	public TradingMachineFixInitiatorApplication(final SessionSettings settings) throws JMSException, FileNotFoundException, IOException {
		this.settings = settings;
		final Properties p = Utility.getApplicationProperties("tradingMachineOrderRouter.properties");
		orderManager = new OrderManager();
		loggedOnSessions = new HashSet<SessionID>();
		activeMQOrdersConsumerConfiguration = Utility.createConsumer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.ordersQueue"), DestinationType.Queue);
		activeMQOrdersConsumerConfiguration.getConsumer().setMessageListener(this);
		activeMQOrdersConsumerConfiguration.getConnection().setExceptionListener(this);
		activeMQOrdersProducerConfiguration = Utility.createProducer(p.getProperty("activeMQ.url"), p.getProperty("activeMQ.filledOrdersTopic"), DestinationType.Topic);
	}
	
	@Override
	public void onCreate(final SessionID sessionId) {
		logger.info("Session created: "+sessionId);
	}

	@Override
	public void onLogon(final SessionID sessionId) {
		logger.info("Logon: "+sessionId);
		loggedOnSessions.add(sessionId);
	}

	@Override
	public void onLogout(final SessionID sessionId) {
		logger.info("Logon: "+sessionId);
		loggedOnSessions.remove(sessionId);
	}

	@Override
	public void toAdmin(final Message message, final SessionID sessionId) {
		try {
			if(MsgType.LOGON.compareTo(message.getHeader().getString(MsgType.FIELD)) == 0)
			{
				final Dictionary dict = settings.get(sessionId);
				message.setString(quickfix.field.Username.FIELD, dict.getString("UserName"));
				message.setString(quickfix.field.Password.FIELD, dict.getString("Password"));
			}	
		}
		catch(final Exception e) {
			logger.warn("Error setting user/ password.");
			e.printStackTrace();
		}
	}

	@Override
	public void fromAdmin(final Message message, final SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		
	}

	@Override
	public void toApp(final Message message, final SessionID sessionId) throws DoNotSend {
	}

	@Override
	public void fromApp(final Message message, final SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		try {
            final MsgType msgType = new MsgType();
            if (message.getHeader().getField(msgType).valueEquals("8")) {
                executionReport(message, sessionId);
            }
        } catch (final Exception e) {
        	logger.warn(e.getMessage());
            e.printStackTrace();
        }

	}
	
	public void send(final SimpleOrder order) {
        final quickfix.fix50.NewOrderSingle newOrderSingle = new quickfix.fix50.NewOrderSingle(
                new ClOrdID(order.getID()), order.getSide().toFIXSide(),
                new TransactTime(), order.getType().toFIXOrderType());
        newOrderSingle.set(new OrderQty(order.getQuantity()));
        newOrderSingle.set(new Symbol(order.getSymbol()));
        newOrderSingle.set(new HandlInst('1'));
        switch(order.getType()) {
        case LIMIT: 
        	newOrderSingle.setField(new Price(order.getLimit()));
        	break;
        case STOP:
        	newOrderSingle.setField(new StopPx(order.getStop()));
        	break;
		default:
			break;
        }
        //else market order.
        newOrderSingle.setField(order.getTimeInForce().toFIXTimeInForce());
        try {
            Session.sendToTarget(newOrderSingle, order.getSessionID());
            orderManager.add(order);
            logger.info("Sent "+order);
        } catch (final SessionNotFound e) {
        	logger.warn("Unable to send order", e);
        }
    }
	
	private void executionReport(Message message, SessionID sessionID) throws FieldNotFound, JMSException {
        final SimpleOrder order = orderManager.getOrder(message.getField(new ClOrdID()).getValue());
        if (order == null) {
            return;
        }
        try {
            order.setMessage(message.getField(new Text()).getValue());
        } catch (final FieldNotFound e) {
        }
        BigDecimal fillSize;
        final LeavesQty leavesQty = new LeavesQty();
        message.getField(leavesQty);
        fillSize = new BigDecimal(order.getQuantity()).subtract(new BigDecimal(leavesQty.getValue()));

        if (fillSize.compareTo(BigDecimal.ZERO) > 0) {
        	//execution.
            order.setOpen(order.getOpen() - (int) Double.parseDouble(fillSize.toPlainString()));
            order.setExecuted(new Integer(message.getString(CumQty.FIELD)));
            order.setAvgPx(new Double(message.getString(AvgPx.FIELD)));
        }

        final char ordStatus = ((OrdStatus) message.getField(new OrdStatus())).getValue();
        switch(ordStatus) {
        case OrdStatus.REJECTED: 
        	order.setRejected(true);
            order.setOpen(0);
            break;
        case OrdStatus.CANCELED:
        case OrdStatus.DONE_FOR_DAY:
        	order.setCanceled(true);
            order.setOpen(0);
            break;
        case OrdStatus.NEW:
        	if (order.isNew()) {
                order.setNew(false);
            }
        	break;
        }
        if (ordStatus == OrdStatus.FILLED)
        	activeMQOrdersProducerConfiguration.getProducer().send(activeMQOrdersProducerConfiguration.getSession().createObjectMessage(order));
        orderManager.updateOrder(order);
    }

	@Override
	public void onMessage(final javax.jms.Message message) {
		if (message instanceof ObjectMessage)
			try {
				loggedOnSessions.forEach(sessionID -> {
					try {
						final SimpleOrder order = (SimpleOrder)((ObjectMessage)message).getObject();
						order.setSessionID(sessionID);
						send(order);
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				});
			} catch (final Exception e) {
				logger.warn("Error receiving order.\n"+e.getMessage());
			}
	}
	 
	@Override
	public void onException(final JMSException jmsEx) {
		logger.warn(jmsEx.getMessage());
	}
	
	public void closeOrdersConsumer() {
		activeMQOrdersConsumerConfiguration.close();
	}
}
