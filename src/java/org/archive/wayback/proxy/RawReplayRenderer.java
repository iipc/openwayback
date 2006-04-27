/* ReplayRenderer
 *
 * $Id$
 *
 * Created on 5:50:38 PM Oct 31, 2005.
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
package org.archive.wayback.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.BaseReplayRenderer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class RawReplayRenderer extends BaseReplayRenderer {

	private final static String HTTP_LOCATION_HEADER = "Location";

	private final static int BYTE_BUFFER_SIZE = 4 * 1024;
	// reusable buffer for copying data to clients:
	private byte[] buffer = new byte[BYTE_BUFFER_SIZE];

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		resource.parseHeaders();
		copyRecordHttpHeader(httpResponse, resource, uriConverter, result,
				false);
		copy(resource, httpResponse.getOutputStream());
	}

	/**
	 * callback function for each HTTP header. If null is returned, header is
	 * omitted from final response to client, otherwise, the possibly modified
	 * http header value is returned to the client.
	 * 
	 * @param key
	 * @param value
	 * @param uriConverter
	 * @param result
	 * @return String
	 */
	protected String filterHeader(final String key, final String value,
			final ResultURIConverter uriConverter, SearchResult result) {
		String finalHeaderValue = value;
		if (0 == key.indexOf(HTTP_LOCATION_HEADER)) {
			finalHeaderValue = uriConverter
					.makeRedirectReplayURI(result, value);
		}
		return finalHeaderValue;
	}

	protected void copyRecordHttpHeader(HttpServletResponse response,
			Resource resource, ResultURIConverter uriConverter,
			SearchResult result, boolean noLength) throws IOException {
		Properties headers = resource.getHttpHeaders();
		int code = resource.getStatusCode();
		// Only return legit status codes -- don't return any minus
		// codes, etc.
		if (code <= HttpServletResponse.SC_CONTINUE) {
			String identifier = "";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Bad status code " + code + " (" + identifier + ").");
			return;
		}
		response.setStatus(code);
		if (headers != null) {
			// Copy all headers to the response -- even date and
			// server, but don't copy Content-Length if arguments indicate
			for (Enumeration e = headers.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				String value = (String) headers.get(key);
				String finalValue = value;
				if (value != null) {
					finalValue = filterHeader(key, value, uriConverter, result);
					if (finalValue == null) {
						continue;
					}
				}
				response.setHeader(key, (finalValue == null) ? "" : finalValue);
			}
		}
	}

	protected void copy(InputStream is, OutputStream os) throws IOException {
		for (int r = -1; (r = is.read(buffer, 0, BYTE_BUFFER_SIZE)) != -1;) {
			os.write(buffer, 0, r);
		}
	}

}
