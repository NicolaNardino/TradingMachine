package com.projects.tradingMachine.utility.order;

import java.io.Serializable;
import java.util.Date;

import quickfix.SessionID;

public class SimpleOrder implements Serializable {

	private static final long serialVersionUID = 1L;
	private SessionID sessionID = null;
    private String symbol = null;
    private int quantity = 0;
    private int open = 0;
    private int executed = 0;
    private OrderSide side = OrderSide.BUY;
    private OrderType type = OrderType.MARKET;
    private OrderTimeInForce timeInForce = OrderTimeInForce.DAY;
    private Double limitPrice = null;
    private Double stopPrice = null;
    private double avgPx = 0.0;
    private boolean rejected = false;
    private boolean canceled = false;
    private boolean isNew = true;
    private String message = null;
    private String ID = null;
    private String originalID = null;
    private static int nextID = 1;
    
    private Date fillDate;
    
    public SimpleOrder() {
        ID = Long.valueOf(System.currentTimeMillis() + (nextID++)).toString();
    }

    /**
     * This constructor is used when the order gets retrieved from the back-end.
     * */
    public SimpleOrder(final String ID, final String symbol, final int quantity, final OrderSide side, final OrderType type, 
    		final OrderTimeInForce timeInForce, final Double limitPrice, final Double stopPrice, final Double price, final String originalID, final Date fillDate) {
    	this.ID = ID;
    	this.symbol = symbol;
    	this.quantity = quantity;
    	this.side = side;
    	this.type = type;
    	this.timeInForce = timeInForce;
    	this.limitPrice = limitPrice;
    	this.stopPrice = stopPrice;
    	this.avgPx = price;
    	this.originalID = originalID;
    	this.fillDate = fillDate;
    }
    
    public SimpleOrder(final String ID) {
        this.ID = ID;
    }
    
    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(final SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(final String symbol) {
        this.symbol = symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(final int open) {
        this.open = open;
    }

    public int getExecuted() {
        return executed;
    }

    public void setExecuted(final int executed) {
        this.executed = executed;
    }

    public OrderSide getSide() {
        return side;
    }

    public void setSide(final OrderSide side) {
        this.side = side;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(final OrderType type) {
        this.type = type;
    }

    public OrderTimeInForce getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(final OrderTimeInForce timeInForce) {
        this.timeInForce = timeInForce;
    }

    public Double getLimit() {
        return limitPrice;
    }

    public void setLimit(final Double limit) {
        this.limitPrice = limit;
    }

    public void setLimit(final String limit) {
        if (limit == null || limit.equals("")) {
            this.limitPrice = null;
        } else {
            this.limitPrice = new Double(limit);
        }
    }

    public Double getStop() {
        return stopPrice;
    }

    public void setStop(final Double stop) {
        this.stopPrice = stop;
    }

    public void setStop(final String stop) {
        if (stop == null || stop.equals("")) {
            this.stopPrice = null;
        } else {
            this.stopPrice = new Double(stop);
        }
    }

    public void setAvgPx(final double avgPx) {
        this.avgPx = avgPx;
    }

    public double getAvgPx() {
        return avgPx;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }

    public boolean getRejected() {
        return rejected;
    }

    public void setCanceled(final boolean canceled) {
        this.canceled = canceled;
    }

    public boolean getCanceled() {
        return canceled;
    }

    public void setNew(final boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setID(final String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setOriginalID(final String originalID) {
        this.originalID = originalID;
    }

    public String getOriginalID() {
        return originalID;
    }
    
    public Date getFillDate() {
    	return fillDate;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		long temp;
		temp = Double.doubleToLongBits(avgPx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (canceled ? 1231 : 1237);
		result = prime * result + executed;
		result = prime * result + (isNew ? 1231 : 1237);
		result = prime * result + ((limitPrice == null) ? 0 : limitPrice.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + open;
		result = prime * result + ((originalID == null) ? 0 : originalID.hashCode());
		result = prime * result + quantity;
		result = prime * result + (rejected ? 1231 : 1237);
		result = prime * result + ((sessionID == null) ? 0 : sessionID.hashCode());
		result = prime * result + ((side == null) ? 0 : side.hashCode());
		result = prime * result + ((stopPrice == null) ? 0 : stopPrice.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		result = prime * result + ((timeInForce == null) ? 0 : timeInForce.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleOrder other = (SimpleOrder) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		if (Double.doubleToLongBits(avgPx) != Double.doubleToLongBits(other.avgPx))
			return false;
		if (canceled != other.canceled)
			return false;
		if (executed != other.executed)
			return false;
		if (isNew != other.isNew)
			return false;
		if (limitPrice == null) {
			if (other.limitPrice != null)
				return false;
		} else if (!limitPrice.equals(other.limitPrice))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (open != other.open)
			return false;
		if (originalID == null) {
			if (other.originalID != null)
				return false;
		} else if (!originalID.equals(other.originalID))
			return false;
		if (quantity != other.quantity)
			return false;
		if (rejected != other.rejected)
			return false;
		if (sessionID == null) {
			if (other.sessionID != null)
				return false;
		} else if (!sessionID.equals(other.sessionID))
			return false;
		if (side != other.side)
			return false;
		if (stopPrice == null) {
			if (other.stopPrice != null)
				return false;
		} else if (!stopPrice.equals(other.stopPrice))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		if (timeInForce != other.timeInForce)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Order [sessionID=" + sessionID + ", symbol=" + symbol + ", quantity=" + quantity + ", open=" + open
				+ ", executed=" + executed + ", side=" + side + ", type=" + type + ", timeInForce=" + timeInForce
				+ ", limit=" + limitPrice + ", stop=" + stopPrice + ", avgPx=" + avgPx + ", rejected=" + rejected + ", canceled="
				+ canceled + ", isNew=" + isNew + ", message=" + message + ", ID=" + ID + ", originalID=" + originalID
				+ "]";
	}
}