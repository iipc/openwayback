/* DomainPrefixReplayRenderer
 *
 * $Id$
 *
 * Created on 10:21:04 AM Aug 10, 2007.
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
package org.archive.wayback.domainprefix;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.replay.HTMLPage;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.HttpHeaderOperation;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixReplayRenderer implements ReplayRenderer, HttpHeaderProcessor {
	private final static String HTTP_LENGTH_HEADER = "Content-Length";
	private final static String HTTP_LENGTH_HEADER_UP = 
		HTTP_LENGTH_HEADER.toUpperCase();

	private final static String HTTP_LOCATION_HEADER = "Location";
	private final static String HTTP_LOCATION_HEADER_UP = 
		HTTP_LOCATION_HEADER.toUpperCase();
	
	private final static Pattern httpPattern = 
		Pattern.compile("(http://[^/]*/)");

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResults)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter, SearchResults results)
			throws ServletException, IOException, BadContentException {
		
		HttpHeaderOperation.copyHTTPMessageHeader(resource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				resource, result, uriConverter, this);

		// Load content into an HTML page, and resolve load-time URLs:
		HTMLPage page = new HTMLPage(resource,result,uriConverter);
		page.readFully();

		String resourceTS = result.getCaptureDate();
		String captureTS = Timestamp.parseBefore(resourceTS).getDateStr();
		
		
		StringBuilder sb = page.sb;
		StringBuffer replaced = new StringBuffer(sb.length());
		Matcher m = httpPattern.matcher(sb);
		while(m.find()) {
			String host = m.group(1);
			String replacement = uriConverter.makeReplayURI(captureTS,host);
			m.appendReplacement(replaced, replacement);
		}
		m.appendTail(replaced);
		byte b[] = replaced.toString().getBytes(page.getCharSet());
		int bytes = b.length;
		headers.put(HTTP_LENGTH_HEADER, String.valueOf(bytes));

		HttpHeaderOperation.sendHeaders(headers, httpResponse);
		httpResponse.getOutputStream().write(b);

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
		if (keyUp.startsWith(HTTP_LOCATION_HEADER_UP)) {

			String baseUrl = result.getAbsoluteUrl();
			String cd = result.getCaptureDate();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);

			output.put(key, uriConverter.makeReplayURI(cd,u));

		} else {
			// others go out as-is:

			output.put(key, value);
		}		
	}
}
