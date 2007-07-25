/* ReplayRenderer
 *
 * $Id$
 *
 * Created on 5:27:09 PM Nov 1, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface ReplayRenderer {

	/**
	 * return a page to the client indicating that something went wrong, and
	 * describing in as much detail as available, what went wrong, in a format
	 * appropriate to the context in which the resource was requested.
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param exception
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException;

	/**
	 * return a resource to the user.
	 * 
	 * @param httpRequest the HttpServletRequest
	 * @param httpResponse the HttpServletResponse
	 * @param wbRequest the WaybackRequest that returned the results
	 * @param result actual SearchResult that maps to resource to replay
	 * @param resource resource to replay
	 * @param uriConverter the URI converter to use to translate matching
	 *                      results into replayable URLs
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource, 
			ResultURIConverter uriConverter) throws ServletException,
			IOException;
}
