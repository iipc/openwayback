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
package org.archive.wayback.replay;

import java.io.IOException;
import java.io.OutputStream;
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

/**
 * ReplayRenderer implementation which returns the archive document as 
 * pristinely as possible -- no modifications to response code, HTTP headers,
 * or original byte-stream.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TransparentReplayRenderer implements ReplayRenderer {
	private HttpHeaderProcessor httpHeaderProcessor;
	private final static int BUFFER_SIZE = 4096;
	public TransparentReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		this.httpHeaderProcessor = httpHeaderProcessor;
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
					throws ServletException, IOException, BadContentException {
		renderResource(httpRequest, httpResponse, wbRequest, result, resource,
				resource, uriConverter, results);
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException, IOException,
			BadContentException {

		HttpHeaderOperation.copyHTTPMessageHeader(httpHeadersResource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				httpHeadersResource, result, uriConverter, httpHeaderProcessor);

		// HACKHACK: getContentLength() may not find the original content length
		// if a HttpHeaderProcessor has mangled it too badly. Should this
		// happen in the HttpHeaderProcessor itself?
		String origLength = HttpHeaderOperation.getContentLength(headers);
		if(origLength != null) {
			headers.put(HttpHeaderOperation.HTTP_LENGTH_HEADER, origLength);
		}

		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		// and copy the raw byte-stream.
		OutputStream os = httpResponse.getOutputStream();
		byte[] buffer = new byte[BUFFER_SIZE];
		long total = 0;
		for (int r = -1; (r = payloadResource.read(buffer, 0, BUFFER_SIZE)) != -1;) {
			os.write(buffer, 0, r);
			total += r;
		}
		if(total == 0) {
			if(headers.size() == 0) {
				// totally empty response
				httpResponse.setContentLength(0);
			}
		}
	}
}
