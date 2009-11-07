/* ResourceNotInArchiveException
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
