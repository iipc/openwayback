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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.PerfWritingHttpServletResponse;

/**
 * Class which encapsulates all Replay context information needed to execute
 * a .jsp file in the "context" of a particular replay request.
 * 
 * This class then manages converting a jsp path into the String it produces.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class JSPExecutor {

	private HttpServletRequest httpRequest = null;
	private HttpServletResponse httpResponse = null;
	private UIResults uiResults = null;
	
	private boolean isAjax = false;
	
	public JSPExecutor(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, UIResults uiResults) {
		this.isAjax = uiResults.getWbRequest().isAjaxRequest();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
		this.uiResults = uiResults;
	}

	/**
	 * initializes JSPExecutor with new {@code UIResults} object.
	 * @param uriConverter ResultURIConverter, passed to {@code UIResults}
	 * @param httpRequest HttpServletRequest
	 * @param httpResponse HttpServletResponse
	 * @param wbRequest WaybackRequest, passed to {@code UIResults}
	 * @param results CaptureSearchResults, passed to {@code UIResults}
	 * @param result CaptureSearchResult being rendered, passed to {@code UIResults}
	 * @param resource Resource being rendered, passed to {@code UIResults}
	 * @deprecated 2014-05-02 use {@link #JSPExecutor(HttpServletRequest, HttpServletResponse, UIResults)}
	 *   passing explicitly created {@code UIResults}
	 */
	public JSPExecutor(ResultURIConverter uriConverter,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, CaptureSearchResults results, 
			CaptureSearchResult result, Resource resource) {
		this(httpRequest, httpResponse, new UIResults(wbRequest, uriConverter,
				results, result, resource));
	}
	
	
	public String jspToString(String jspPath) 
	throws ServletException, IOException {
		
		// If ajax request, don't do any jsp insertion
		if (isAjax) {
			return "";
		}
		
		if (httpResponse instanceof PerfWritingHttpServletResponse) {
			uiResults.setPerfResponse((PerfWritingHttpServletResponse)httpResponse);
		}

		StringHttpServletResponseWrapper wrappedResponse = 
			new StringHttpServletResponseWrapper(httpResponse);
		uiResults.forward(httpRequest, wrappedResponse, jspPath);
		return wrappedResponse.getStringResponse();
	}
	
	public UIResults getUiResults()
	{
		return uiResults;
	}

}
