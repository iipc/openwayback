/* QueryRenderer
 *
 * $Id$
 *
 * Created on 2:47:42 PM Nov 7, 2005.
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
package org.archive.wayback.query;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.WaybackException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Renderer implements QueryRenderer {
	private final static String JSP_PATH = "queryui.jsppath";

	private String jspPath = null;

	private final String ERROR_JSP = "ErrorResult.jsp";

	private final String QUERY_JSP = "QueryResults.jsp";

	private final String PREFIX_QUERY_JSP = "PathQueryResults.jsp";

	public void init(Properties p) throws ConfigurationException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new ConfigurationException("Failed to find " + JSP_PATH);
		}
	}

	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		httpRequest.setAttribute("exception", exception);

		String finalJspPath = jspPath + "/" + ERROR_JSP;

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);

		dispatcher.forward(httpRequest, httpResponse);
	}

	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResults results, ReplayResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIQueryResults uiResults;
		try {
			uiResults = new UIQueryResults(httpRequest, wbRequest, results,
					uriConverter);
		} catch (ParseException e) {
			// I don't think this should happen...
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}

		httpRequest.setAttribute("ui-results", uiResults);
		proxyRequest(httpRequest, httpResponse, QUERY_JSP);

	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.QueryRenderer#renderUrlPrefixResults(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResults, org.archive.wayback.ReplayResultURIConverter)
	 */
	public void renderUrlPrefixResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResults results, ReplayResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIQueryResults uiResults;
		try {
			uiResults = new UIQueryResults(httpRequest, wbRequest, results,
					uriConverter);
		} catch (ParseException e) {
			// I don't think this should happen...
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}

		httpRequest.setAttribute("ui-results", uiResults);
		proxyRequest(httpRequest, httpResponse, PREFIX_QUERY_JSP);

	}

	/**
	 * @param request
	 * @param response
	 * @param jspName
	 * @throws ServletException
	 * @throws IOException
	 */
	private void proxyRequest(HttpServletRequest request,
			HttpServletResponse response, final String jspName)
			throws ServletException, IOException {

		String finalJspPath = jspPath + "/" + jspName;

		RequestDispatcher dispatcher = request
				.getRequestDispatcher(finalJspPath);
		dispatcher.forward(request, response);
	}
}
