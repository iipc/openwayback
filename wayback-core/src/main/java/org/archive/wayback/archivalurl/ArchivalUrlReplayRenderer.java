/* ArchivalUrlReplayRenderer
 *
 * $Id$
 *
 * Created on 6:11:00 PM Aug 8, 2007.
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
package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.replay.HTMLPage;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.HttpHeaderOperation;
import org.archive.wayback.util.url.UrlOperations;

/**
 * ReplayRenderer responsible for marking up HTML pages so they replay in
 * ArchivalUrl context:
 *   resolve in page URLs
 *   add HTML comment and javascript to modify URLs client-side to point back
 *       to this context 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlReplayRenderer implements ReplayRenderer, HttpHeaderProcessor {


	private List<String> jsInserts = null;
	private List<String> jspInserts = null;
	private boolean serverSideRendering = false;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResults)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter, SearchResults results)
			throws ServletException, IOException, BadContentException {

		StringBuilder toInsert = new StringBuilder(300);

		HttpHeaderOperation.copyHTTPMessageHeader(resource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				resource, result, uriConverter, this);

	
		// Load content into an HTML page, and resolve load-time URLs:
		HTMLPage page = new HTMLPage(resource,result,uriConverter);
		page.readFully();
		
		if(serverSideRendering) {
			page.resolveAllPageUrls();
		} else {
			page.resolvePageUrls();
		}
		if(jsInserts != null) {
			Iterator<String> itr = jsInserts.iterator();
			while(itr.hasNext()) {
				toInsert.append(page.getJSIncludeString(itr.next()));
			}
		}
		if(jspInserts != null) {
			Iterator<String> itr = jspInserts.iterator();
			while(itr.hasNext()) {
				toInsert.append(page.includeJspString(itr.next(), httpRequest, 
						httpResponse, wbRequest, results, result));
			}
		}

		// insert the new content:
		if(serverSideRendering) {
			page.insertAtStartOfBody(toInsert.toString());
		} else {
			page.insertAtEndOfBody(toInsert.toString());
		}
		
		// set the corrected length:
		int bytes = page.getBytes().length;
		headers.put(HTTP_LENGTH_HEADER, String.valueOf(bytes));
		// Tomcat will always send a charset... It's trying to be smarter than
		// we are. If the original page didn't include a "charset" as part of
		// the "Content-Type" HTTP header, then Tomcat will use the default..
		// who knows what that is, or what that will do to the page..
		// let's try explicitly setting it to what we used:
		httpResponse.setCharacterEncoding(page.getCharSet());

		// send back the headers:
		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		page.writeToOutputStream(httpResponse.getOutputStream());
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HeaderFilter#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, SearchResult result) {

		String keyUp = key.toUpperCase();

		// omit Content-Length header
		if (keyUp.equals(HTTP_LENGTH_HEADER_UP)) {
			return;
		}

		// rewrite Location header URLs
		if (keyUp.startsWith(HTTP_LOCATION_HEADER_UP) ||
				keyUp.startsWith(HTTP_CONTENT_BASE_HEADER_UP)) {

			String baseUrl = result.getAbsoluteUrl();
			String cd = result.getCaptureDate();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);

			output.put(key, uriConverter.makeReplayURI(cd,u));
		} else if(keyUp.startsWith(HTTP_CONTENT_TYPE_HEADER_UP)) {
			output.put("X-Wayback-Orig-" + key,value);
			output.put(key,value);
		} else {
			// others go out as-is:

			output.put(key, value);
		}
	}

	/**
	 * @return the jsInserts
	 */
	public List<String> getJsInserts() {
		return jsInserts;
	}

	/**
	 * @param jsInserts the jsInserts to set
	 */
	public void setJsInserts(List<String> jsInserts) {
		this.jsInserts = jsInserts;
	}

	/**
	 * @return the jspInserts
	 */
	public List<String> getJspInserts() {
		return jspInserts;
	}

	/**
	 * @param jspInserts the jspInserts to set
	 */
	public void setJspInserts(List<String> jspInserts) {
		this.jspInserts = jspInserts;
	}

	/**
	 * @return the isServerSideRendering
	 */
	public boolean isServerSideRendering() {
		return serverSideRendering;
	}

	/**
	 * @param isServerSideRendering the isServerSideRendering to set
	 */
	public void setServerSideRendering(boolean serverSideRendering) {
		this.serverSideRendering = serverSideRendering;
	}
}
