/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
