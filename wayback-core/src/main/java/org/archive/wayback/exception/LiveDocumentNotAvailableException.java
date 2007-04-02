/* LiveDocumentNotAvailableException
 *
 * $Id$
 *
 * Created on 4:53:36 PM Mar 12, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.exception;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LiveDocumentNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceIndexNotAvailable";
	protected static final String defaultMessage = "Live document unavailable";
	/**
	 * Constructor
	 * @param url 
	 * @param code 
	 */
	public LiveDocumentNotAvailableException(URL url, int code) {
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)",defaultMessage);
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * @param url 
	 * @param code 
	 * @param details
	 */
	public LiveDocumentNotAvailableException(URL url, int code, String details){
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)",defaultMessage,details);
		id = ID;
	}
	/**
	 * @param url
	 */
	public LiveDocumentNotAvailableException(String url){
		super("The URL " + url + " is not available",defaultMessage);
		id = ID;
	}
	
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_BAD_GATEWAY;
	}
}
