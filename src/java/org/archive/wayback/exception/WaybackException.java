/* WaybackException
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Base class for Wayback internal exceptions.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String GENERIC_ID = "wayback";
	protected static final String KEY_PREFIX = "Exception.";
	protected static final String KEY_TITLE_SUFFIX = ".title";
	protected static final String KEY_MESSAGE_SUFFIX = ".message";
	private String message = "";
	private String title = "Wayback Exception";
	private String details = "";
	protected String id = GENERIC_ID;
	
	/**
	 * Constructor with message only
	 * 
	 * @param message
	 */
	public WaybackException(String message) {
		super(message);
		this.message = message;
	}
	
	/**
	 * Constructor with message, and title
	 * 
	 * @param message
	 * @param title
	 */
	public WaybackException(String message, String title) {
		super(message);
		this.message = message;
		this.title= title;
	}
	
	/**
	 * Constructor with message, title, and details
	 * 
	 * @param message
	 * @param title
	 * @param details
	 */
	public WaybackException(String message, String title,  String details) {
		super(message);
		this.message = message;
		this.title= title;
		this.details = details;
	}
	
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return Returns the details.
	 */
	public String getDetails() {
		return details;
	}
	/**
	 * @return The localization key name of the title of this WaybackException
	 */
	public String getTitleKey() {
		return KEY_PREFIX + id + KEY_TITLE_SUFFIX;
	}
	/**
	 * @return The localization key name of the message of this WaybackException
	 */
	public String getMessageKey() {
		return KEY_PREFIX + id + KEY_MESSAGE_SUFFIX;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
}
