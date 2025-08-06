package com.arquitectura.error;



public class NotRetryableException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotRetryableException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public NotRetryableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	
}
