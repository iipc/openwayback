package org.archive.wayback.exception;

public class ResourceNotInArchiveException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotInArchiveException(String message) {
		super(message,"Not in Archive");
	}
	public ResourceNotInArchiveException(String message,String details) {
		super(message,"Not in Archive",details);
	}
}
