/* QueryRenderer
 *
 * $Id$
 *
 * Created on 2:39:48 PM Nov 7, 2005.
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

import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public interface QueryRenderer {
	
	/** Show the SearchResults of the request for this particular URL
	 *  
	 * @param httpRequest the HttpServletRequest
	 * @param httpResponse the HttpServletResponse
	 * @param wbRequest the WaybackRequest that returned the results
	 * @param results the SearchResults that the WaybackRequest matched
	 * @param uriConverter the URI converter to use to translate matching
	 *                      results into replayable URLs
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderCaptureResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException;

	/** Show the SearchResults of the request which may have resulted in 
	 * multiple matching URLs.
	 * 
	 * @param httpRequest the HttpServletRequest
	 * @param response the HttpServletResponse
	 * @param wbRequest the WaybackRequest that returned the results
	 * @param results the SearchResults that the WaybackRequest matched
	 * @param uriConverter the URI converter to use to translate matching
	 *                      results into replayable URLs
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse response, WaybackRequest wbRequest,
			UrlSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException;

}
