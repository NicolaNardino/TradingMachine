package com.projects.tradingMachine.utility.order;

import java.util.Arrays;
import java.util.Optional;

import quickfix.field.OrdType;

public enum OrderType {
	MARKET("Market"), LIMIT("Limit"), STOP("Stop");
	private final String typeName;
	
    private OrderType(final String name) {
        this.typeName = name;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public OrdType toFIXOrderType() {
    	switch(this) {
    		case MARKET: return new OrdType(OrdType.MARKET);
    		case LIMIT: return new OrdType(OrdType.LIMIT);
    		case STOP: return new OrdType(OrdType.STOP);
    		default: throw new IllegalArgumentException("Unable to convert "+this+" to quickfixj order type.");
    	}
    }
    
    public static OrderType fromString(final String orderType) {
    	final Optional<OrderType> result = Arrays.stream(OrderType.values()).filter(o -> o.typeName.equals(orderType)).findFirst();
    	if (result.isPresent())
    		return result.get();
    	throw new IllegalArgumentException("Unknown order type: "+orderType);
    }
}
