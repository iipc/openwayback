package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class for queries which matching resource is not presently
 * accessible
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceNotAvailable";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotAvailableException(String message) {
		super(message,"Resource not available");
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceNotAvailableException(String message,String details) {
		super(message,"Resource not available",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
