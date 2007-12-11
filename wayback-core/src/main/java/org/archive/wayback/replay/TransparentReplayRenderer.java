/* TransparentReplayRenderer
 *
 * $Id$
 *
 * Created on 5:38:11 PM Aug 8, 2007.
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
package org.archive.wayback.replay;

import java.io.IOException;
import java.io.OutputStream;
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

/**
 * ReplayRenderer implementation which returns the archive document as 
 * pristinely as possible -- no modifications to response code, HTTP headers,
 * or original byte-stream.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TransparentReplayRenderer implements ReplayRenderer, HttpHeaderProcessor {

	private final static int BUFFER_SIZE = 4096;
	
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

		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		// and copy the raw byte-stream.
		OutputStream os = httpResponse.getOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		for (int r = -1; (r = resource.read(buffer, 0, BUFFER_SIZE)) != -1;) {
			os.write(buffer, 0, r);
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HeaderFilter#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, SearchResult result) {

		// copy all HTTP headers, as-is.
		output.put(key, value);
	}
}
