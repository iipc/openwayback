/* ArchivalUrlDateRedirectReplayRenderer
 *
 * $Id$
 *
 * Created on 3:57:54 PM Apr 10, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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
package org.archive.wayback.archivalurl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ArchivalUrlDateRedirectReplayRenderer implements ReplayRenderer {

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResults)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
			throws ServletException, IOException {

		// redirect to the better version:
		String url = result.getOriginalUrl();
		String captureDate = makeFlagDateSpec(
				result.getCaptureTimestamp(),wbRequest);

		String betterURI = uriConverter.makeReplayURI(captureDate,url);
		httpResponse.sendRedirect(betterURI);
	}

	/**
	 * Given a date, and a WaybackRequest object, create a new datespec + flags
	 * which represent the same options as requested by the WaybackRequest
	 * @param timestamp the 14-digit timestamp to use
	 * @param request the WaybackRequest from which o get extra request option 
	 * flags
	 * @return a String representing the flags on the WaybackRequest for the
	 * specified date
	 */
	public static String makeFlagDateSpec(String timestamp, WaybackRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(timestamp);
		if(request.isCSSContext()) {
			sb.append(ArchivalUrlRequestParser.CSS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(request.isJSContext()) {
			sb.append(ArchivalUrlRequestParser.JS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(request.isIMGContext()) {
			sb.append(ArchivalUrlRequestParser.IMG_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(request.isIdentityContext()) {
			sb.append(ArchivalUrlRequestParser.IDENTITY_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		return sb.toString();
	}
}
