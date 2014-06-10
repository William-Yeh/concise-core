package org.sustudio.concise.core.statistics;

/*
 * Exception thrown by the PCA class
 */
public class PCAException extends Exception {
	
	private static final long serialVersionUID = -4751711488813963056L;

	// constructor signatures all match constructors of the Exception class
	public PCAException() {
		super();
	}
	
	public PCAException(String message) {
		super(message);
	}
	
	public PCAException(String message, Throwable cause) {
		super(message,cause);
	}
	
	public PCAException(Throwable cause) {
		super(cause);
	}
        
}
