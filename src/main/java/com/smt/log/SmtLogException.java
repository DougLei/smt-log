package com.smt.log;

/**
 * 
 * @author DougLei
 */
public class SmtLogException extends RuntimeException {

	public SmtLogException() {}
	public SmtLogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	public SmtLogException(String message, Throwable cause) {
		super(message, cause);
	}
	public SmtLogException(String message) {
		super(message);
	}
	public SmtLogException(Throwable cause) {
		super(cause);
	}
}
