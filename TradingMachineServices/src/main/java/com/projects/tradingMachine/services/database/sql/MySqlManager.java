package com.projects.tradingMachine.services.database.sql;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.projects.tradingMachine.services.database.DataManager;
import com.projects.tradingMachine.services.database.DatabaseProperties;
import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderTimeInForce;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

public class MySqlManager implements DataManager {
	private static Logger logger = LoggerFactory.getLogger(MySqlManager.class);

	private final MySqlConnection mySqlConnection;
	
	public MySqlManager(final MySqlConnection mySqlConnection) {
		this.mySqlConnection = mySqlConnection;
	}

	@Override
	public void storeOrder(final SimpleOrder order) {
		try(final CallableStatement stm = mySqlConnection.getConnection().prepareCall("{call addOrder(?,?,?,?,?,?,?,?,?)}")) {
			stm.setString(1, order.getID());
			stm.setString(2, order.getSymbol());
			stm.setInt(3, order.getQuantity());
			stm.setString(4, order.getSide().toString());
			stm.setString(5, order.getType().toString());
			stm.setString(6, order.getTimeInForce().toString());
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
		logger.info("Starting to get orders data...");
		final List<SimpleOrder> result = new ArrayList<SimpleOrder>();
		try(final CallableStatement stm = mySqlConnection.getConnection().prepareCall("{call getOrders (?)}")) {
			if (orderType.isPresent())
				stm.setString(1, orderType.get().toString());
			else
				stm.setNull(1, java.sql.Types.VARCHAR);
			final ResultSet rs = stm.executeQuery();
			while (rs.next())
				result.add(new SimpleOrder(rs.getString("ID"), rs.getString("symbol"), rs.getInt("quantity"), OrderSide.fromString(rs.getString("side")), 
						OrderType.fromString(rs.getString("type")), OrderTimeInForce.fromString(rs.getString("time_in_force")), 
						rs.getDouble("limit_price"), rs.getDouble("stop_price"), rs.getDouble("price"), rs.getString("original_id"), rs.getDate("fill_date"))); 
		}
		catch(final SQLException e) {
			throw new RuntimeException(e);
		}
		logger.info("Number of orders retrieved: "+result.size());
		return result;
	}
	
	@Override
	public void close() throws Exception {
		mySqlConnection.close();
	}
	
	public static void main(final String[] args) throws NumberFormatException, ClassNotFoundException, SQLException, Exception {
		final Properties p = Utility.getApplicationProperties("tradingMachineServices.properties"); 
		try(final DataManager mySqlManager = new MySqlManager(new MySqlConnection(new DatabaseProperties(p.getProperty("mySQL.host"), Integer.valueOf(p.getProperty("mySQL.port")), p.getProperty("mySQL.database"), 
				p.getProperty("mySQL.userName"), p.getProperty("mySQL.password"))))) {
			//System.out.println(mySqlManager.getOrders(Optional.of(OrderType.STOP)).stream().mapToDouble(SimpleOrder::getAvgPx).summaryStatistics());
			System.out.println(mySqlManager.getOrders(Optional.ofNullable(null)).stream().mapToDouble(SimpleOrder::getAvgPx).summaryStatistics());
			//mongoDBManager.getOrders(Optional.of(OrderType.LIMIT)).stream().map(SimpleOrder::getAvgPx).forEach(System.out::println);
		}
	}
}