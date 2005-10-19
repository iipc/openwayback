/* RequestParser
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

package org.archive.wayback;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WMRequest;

/**
 * Parser of user requests from URL, query argument, Cookies, sessionID, etc
 * into a WMRequest object.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public interface RequestParser {
	/**
	 * Attempt to extract a valid WMRequest object from the HttpServletRequest
	 * 
	 * @param request
	 * @return null or the parsed WMRequest object representing the users
	 *         request.
	 */
	public WMRequest parseRequest(final HttpServletRequest request);
}
