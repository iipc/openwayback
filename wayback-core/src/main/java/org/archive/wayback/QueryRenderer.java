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
package org.archive.wayback;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * {@code QueryRenderer} generates response for two types of resource index
 * query: <em>capture search</em> and <em>URL search</em>.
 *
 * <h3>REFACTORING THOUGHTS</h3>
 * <p>There's no good reason two types of query should be bundled in one object.
 * Probably we should split this interface into two interfaces, or for the better,
 * just one interface covering the two. It'll open up a possibility of unifying
 * {@link ReplayRenderer} and {@code QueryRendere} into one interface to simplify
 * {@code AccessPoint}.
 * </p>
 * @author brad
 * @see ReplayRenderer
 */
public interface QueryRenderer {

	/**
	 * Display matching SearchResults for the WaybackRequest to the user.
	 *
	 * @param httpRequest the HttpServletRequest
	 * @param httpResponse the HttpServletResponse
	 * @param wbRequest the WaybackRequest that returned the results
	 * @param results the SearchResults that the WaybackRequest matched
	 * @param uriConverter the URI converter to use to translate matching
	 *        results into replayable URLs
	 * @throws ServletException per usual
	 * @throws IOException per usual
	 */
	public void renderCaptureResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException;

	/**
	 * Show the SearchResults of the request which may have resulted in multiple
	 * matching URLs.
	 *
	 * @param httpRequest the HttpServletRequest
	 * @param response the HttpServletResponse
	 * @param wbRequest the WaybackRequest that returned the results
	 * @param results the SearchResults that the WaybackRequest matched
	 * @param uriConverter the URI converter to use to translate matching
	 *        results into replayable URLs
	 * @throws ServletException per usual
	 * @throws IOException per usual
	 */
	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse response, WaybackRequest wbRequest,
			UrlSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException;

}
