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
	private String message = "";
	private String title = "Wayback Exception";
	private String details = "";
	
	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public WaybackException(String message) {
		super(message);
		this.message = message;
	}
	
	public WaybackException(String message, String title) {
		super(message);
		this.message = message;
		this.title= title;
	}
	
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
}
