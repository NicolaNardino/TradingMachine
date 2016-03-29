package com.projects.tradingMachine.utility.order;

import java.util.Arrays;
import java.util.Optional;

import quickfix.field.Side;

public enum OrderSide {
	BUY("Buy"), SELL("Sell");
	
    private final String sideName;
    
    private OrderSide(final String name) {
    	this.sideName = name;
    }

    @Override
    public String toString() {
        return sideName;
    }
    
    public Side toFIXSide() {
    	switch(this) {
    		case BUY: return new Side(Side.BUY);
    		case SELL: return new Side(Side.SELL);
    		default: throw new IllegalArgumentException("Unable to convert "+this+" to quickfixj order side.");
    	}
    }

    public static OrderSide fromString(final String orderSide) {
    	final Optional<OrderSide> result = Arrays.stream(OrderSide.values()).filter(o -> o.sideName.equals(orderSide)).findFirst();
    	if (result.isPresent())
    		return result.get();
    	throw new IllegalArgumentException("Unknown order side: "+orderSide);
    }
}
