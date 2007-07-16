/* PathRequestParser
 *
 * $Id$
 *
 * Created on 6:47:21 PM Apr 24, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.requestparser;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.WaybackContext;

/**
 * Subclass of RequestParser that acquires key request information from the
 * path component following the wayback context. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class PathRequestParser extends BaseRequestParser {

	/**
	 * @param requestPath
	 * @return WaybackRequest with information parsed from the requestPath, or
	 * null if information could not be extracted.
	 */
	public abstract WaybackRequest parse(String requestPath);

	/* (non-Javadoc)
	 * @see org.archive.wayback.requestparser.BaseRequestParser#parse(javax.servlet.http.HttpServletRequest, org.archive.wayback.webapp.WaybackContext)
	 */
	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			WaybackContext wbContext) throws BadQueryException {

		String queryString = httpRequest.getQueryString();
		String origRequestPath = httpRequest.getRequestURI();

		if (queryString != null) {
			origRequestPath += "?" + queryString;
		}
		String contextPath = wbContext.getContextPath(httpRequest);
		if (!origRequestPath.startsWith(contextPath)) {
			return null;
		}
		String requestPath = origRequestPath.substring(contextPath.length());
		
		WaybackRequest wbRequest = parse(requestPath);
		if(wbRequest != null) {
			addHttpHeaderFields(wbRequest, httpRequest);
		}

		return wbRequest;
	}
}
