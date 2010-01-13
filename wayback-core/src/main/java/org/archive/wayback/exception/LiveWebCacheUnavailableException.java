/* LiveWebCacheUnavailableException
 *
 * $Id$:
 *
 * Created on Dec 18, 2009.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.exception;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

/**
 * @author brad
 *
 */
public class LiveWebCacheUnavailableException  extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "liveWebCacheNotAvailable";
	protected static final String defaultMessage = "LiveWebCache unavailable";
	/**
	 * Constructor
	 * @param url 
	 * @param code 
	 */
	public LiveWebCacheUnavailableException(URL url, int code) {
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
	public LiveWebCacheUnavailableException(URL url, int code, String details){
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)",defaultMessage,details);
		id = ID;
	}
	/**
	 * @param url
	 */
	public LiveWebCacheUnavailableException(String url){
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
