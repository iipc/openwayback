package org.archive.wayback.exception;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class for queries which result in no index matches
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceNotInArchiveException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceNotInArchive";
	/**
	 * List of alternate string URLs that might get the user what they want.
	 */
	private List<String> closeMatches = null;
	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotInArchiveException(String message) {
		super(message,"Not in Archive");
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceNotInArchiveException(String message,String details) {
		super(message,"Not in Archive",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
	/**
	 * @return the closeMatches
	 */
	public List<String> getCloseMatches() {
		return closeMatches;
	}
	/**
	 * @param closeMatches the closeMatches to set
	 */
	public void setCloseMatches(List<String> closeMatches) {
		this.closeMatches = closeMatches;
	}
}
