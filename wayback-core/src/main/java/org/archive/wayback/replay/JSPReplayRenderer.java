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

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

/**
 * ReplayRenderer implementation which just forwards responsibility for 
 * rendering a resource to a .jsp file.
 * 
 * @author brad
 *
 */
public class JSPReplayRenderer implements ReplayRenderer {
	private String targetJsp = null;

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
					throws ServletException, IOException, WaybackException {
		UIResults uiResults =
				new UIResults(wbRequest, uriConverter, results, result, resource);
		uiResults.forward(httpRequest, httpResponse, 
				targetJsp);
	}

	/**
	 * @return the context-relative path to the .jsp responsible for rendering
	 * the resource
	 */
	public String getTargetJsp() {
		return targetJsp;
	}

	/**
	 * @param targetJsp the context-relative path to the .jsp responsible for
	 * rendering the resource
	 */
	public void setTargetJsp(String targetJsp) {
		this.targetJsp = targetJsp;
	}

	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException, IOException,
			WaybackException {
		renderResource(httpRequest, httpResponse, wbRequest, result,
				payloadResource, uriConverter, results);
	}
}
