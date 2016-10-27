package com.projects.tradingMachine.utility.database.creditCheck;

public class CreditCheckException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CreditCheckException() {}
	
	public CreditCheckException(final String message) {
		super(message);
	}
	
	public CreditCheckException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public CreditCheckException(final Throwable cause) {
		super(cause);
	}
}
