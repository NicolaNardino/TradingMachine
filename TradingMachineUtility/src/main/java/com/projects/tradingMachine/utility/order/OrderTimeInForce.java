package com.projects.tradingMachine.utility.order;

import java.util.Arrays;
import java.util.Optional;

import quickfix.field.TimeInForce;

public enum OrderTimeInForce {
	DAY("Day"), IOC("IOC"), FOK("FOK");

    private final String timeInForceName;

    private OrderTimeInForce(final String timeInForceName) {
        this.timeInForceName = timeInForceName;
    }

    public String toString() {
        return timeInForceName;
    }

    public TimeInForce toFIXTimeInForce() {
    	switch(this) {
    		case DAY: return new TimeInForce(TimeInForce.DAY);
    		case IOC: return new TimeInForce(TimeInForce.IMMEDIATE_OR_CANCEL);
    		case FOK: return new TimeInForce(TimeInForce.FILL_OR_KILL);
    		default: throw new IllegalArgumentException("Unable to convert "+this+" to quickfixj time in force.");
    	}
    }
    
    public static OrderTimeInForce fromString(final String timeInForceName) {
    	final Optional<OrderTimeInForce> result = Arrays.stream(OrderTimeInForce.values()).filter(o -> o.timeInForceName.equals(timeInForceName)).findFirst();
    	if (result.isPresent())
    		return result.get();
    	throw new IllegalArgumentException("Unknown order time in force: "+timeInForceName);
    }
}
