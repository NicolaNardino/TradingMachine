package com.projects.tradingMachine.services.simulation.orders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.projects.tradingMachine.utility.Utility;
import com.projects.tradingMachine.utility.order.OrderSide;
import com.projects.tradingMachine.utility.order.OrderTimeInForce;
import com.projects.tradingMachine.utility.order.OrderType;
import com.projects.tradingMachine.utility.order.SimpleOrder;

/**
 * Orders randomly built. 
 * */
public final class RandomOrdersBuilder {
	private static final Random randomGenerator = new Random();
	
	public static SimpleOrder build(final List<String> allowedSymbols) {
		final SimpleOrder order = new SimpleOrder();	
		order.setSide(randomEnumValue(OrderSide.class));
		final OrderType randomOrderType = randomEnumValue(OrderType.class);
		switch(randomOrderType) {
			case LIMIT: order.setLimit(Utility.roundDouble(randomGenerator.nextDouble() * 100, 2)); break;
			case STOP: order.setStop(Utility.roundDouble(randomGenerator.nextDouble() * 100, 2)); break;
			default: break;
		}
		order.setType(randomOrderType);
		order.setQuantity(randomGenerator.nextInt(1000) + 1);
		order.setSymbol(randomListValue(allowedSymbols));
		order.setTimeInForce(randomEnumValue(OrderTimeInForce.class));
		order.SetFillDate(new Date());
		return order;
	}
	
	private static <T extends Enum<?>> T randomEnumValue(final Class<T> enumClass){
        return enumClass.getEnumConstants()[randomGenerator.nextInt(enumClass.getEnumConstants().length)];
    }
	
	private static <T> T randomListValue(final List<T> list){
	    if(list == null || list.isEmpty()){
	        return null;
	    }
	    return list.get(randomGenerator.nextInt(list.size()));
	}
	
	public static void main(final String[] args) {
		IntStream.range(0, 10).forEach(i -> System.out.println(RandomOrdersBuilder.build(Arrays.asList("RIEN", "UBSN", "CSGN"))));
	}
}
