package org.archive.wayback.exception;


public class ResourceIndexNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceIndexNotAvailableException(String message) {
		super(message,"Index not available");
	}
	public ResourceIndexNotAvailableException(String message, String details) {
		super(message,"Index not available",details);
	}
}
