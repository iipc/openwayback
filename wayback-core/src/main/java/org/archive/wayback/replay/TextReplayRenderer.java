/* HTMLReplayRenderer
 *
 * $Id$
 *
 * Created on 1:07:28 PM Jul 15, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.replay.charset.CharsetDetector;
import org.archive.wayback.replay.charset.StandardCharsetDetector;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class TextReplayRenderer implements ReplayRenderer {

	private List<String> jspInserts = null;
	private HttpHeaderProcessor httpHeaderProcessor;
	private CharsetDetector charsetDetector = new StandardCharsetDetector();

	public TextReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		this.httpHeaderProcessor = httpHeaderProcessor;
	}
	
	protected abstract void updatePage(TextDocument page, 
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
		throws ServletException, IOException;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResults)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
			throws ServletException, IOException, BadContentException {

		HttpHeaderOperation.copyHTTPMessageHeader(resource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				resource, result, uriConverter, httpHeaderProcessor);
	
		String charSet = charsetDetector.getCharset(resource, wbRequest);
		// Load content into an HTML page, and resolve load-time URLs:
		TextDocument page = new TextDocument(resource,result,uriConverter);
		page.readFully(charSet);
		
		updatePage(page,httpRequest,httpResponse,wbRequest,result,resource,
				uriConverter,results);

		// set the corrected length:
		int bytes = page.getBytes().length;
		headers.put(HttpHeaderOperation.HTTP_LENGTH_HEADER, String.valueOf(bytes));
		// Tomcat will always send a charset... It's trying to be smarter than
		// we are. If the original page didn't include a "charset" as part of
		// the "Content-Type" HTTP header, then Tomcat will use the default..
		// who knows what that is, or what that will do to the page..
		// let's try explicitly setting it to what we used:
		headers.put("X-Wayback-Guessed-Charset", page.getCharSet());

		// send back the headers:
		HttpHeaderOperation.sendHeaders(headers, httpResponse);
		httpResponse.setCharacterEncoding(page.getCharSet());

		page.writeToOutputStream(httpResponse.getOutputStream());
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
	 * @return the charsetDetector
	 */
	public CharsetDetector getCharsetDetector() {
		return charsetDetector;
	}

	/**
	 * @param charsetDetector the charsetDetector to set
	 */
	public void setCharsetDetector(CharsetDetector charsetDetector) {
		this.charsetDetector = charsetDetector;
	}
}
