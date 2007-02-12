package org.archive.wayback.exception;

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
}
