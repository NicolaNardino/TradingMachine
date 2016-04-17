package com.projects.tradingMachine.services.database.sql;

import java.sql.CallableStatement;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DataManager;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public class MySqlManager implements DataManager {
	private static Logger logger = LoggerFactory.getLogger(MySqlManager.class);

	private final MySqlConnection mySqlConnection;
	
	public MySqlManager(final MySqlConnection mySqlConnection) {
		this.mySqlConnection = mySqlConnection;
	}

	@Override
	public void storeOrder(SimpleOrder order) {
		try(final CallableStatement stm = mySqlConnection.getConnection().prepareCall("{call addOrder(?,?,?,?,?,?,?,?,?)}")) {
			stm.setString(1, order.getID());
			stm.setString(2, order.getSymbol());
			stm.setInt(3, order.getQuantity());
			stm.setString(4, order.getSide().name());
			stm.setString(5, order.getType().name());
			stm.setString(6, order.getTimeInForce().name());
			switch(order.getType()) {
			case LIMIT: 
				stm.setDouble(7, order.getLimit());
				stm.setNull(8, java.sql.Types.DOUBLE);
				break;
			case STOP: 
				stm.setNull(7, java.sql.Types.DOUBLE);
				stm.setDouble(8, order.getStop());
				break;
			default:
				stm.setNull(7, java.sql.Types.DOUBLE);
				stm.setNull(8, java.sql.Types.DOUBLE);
			}
			stm.setDouble(9, order.getAvgPx());
			stm.execute();
		}
		catch(final Exception ex) {
			logger.warn("Failed to store order "+order+", due to: "+ex.getMessage());
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<SimpleOrder> getOrders(final Optional<OrderType> orderType) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	@Override
	public void close() throws Exception {
		mySqlConnection.close();
	}
}