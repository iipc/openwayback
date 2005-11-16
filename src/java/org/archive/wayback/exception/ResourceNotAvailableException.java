package org.archive.wayback.exception;

public class ResourceNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotAvailableException(String message) {
		super(message,"Resource not available");
	}
	public ResourceNotAvailableException(String message,String details) {
		super(message,"Resource not available",details);
	}
}
