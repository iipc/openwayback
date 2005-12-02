/* QueryServlet
 *
 * $Id$
 *
 * Created on 2:42:50 PM Nov 7, 2005.
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
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackLogic;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.WaybackException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class QueryServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	private WaybackLogic wayback = new WaybackLogic();
	
	private OpenSearchQueryParser qp = new OpenSearchQueryParser();

	/**
	 * Constructor
	 */
	public QueryServlet() {
		super();
	}

	public void init(ServletConfig c) throws ServletException {

		Properties p = new Properties();
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}
		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}

		// TODO initialize renderer?
		wayback.init(p);
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		WaybackRequest wbRequest = (WaybackRequest) httpRequest
				.getAttribute(WMREQUEST_ATTRIBUTE);

		QueryRenderer renderer;
		try {
			renderer = wayback.getQueryRenderer();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}

		try {

			ResourceIndex idx = wayback.getResourceIndex();
			ReplayResultURIConverter uriConverter = wayback.getURIConverter();

			if (wbRequest == null) {
				wbRequest = qp.parseQuery(httpRequest.getParameterMap());
				//wbRequest = parseCGIRequest(httpRequest);
			}

			SearchResults results;

			results = idx.query(wbRequest);

			if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_URL_QUERY)) {
				
				renderer.renderUrlResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);

			} else if (wbRequest.get(WaybackConstants.REQUEST_TYPE).equals(
					WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {
				
				renderer.renderUrlPrefixResults(httpRequest, httpResponse,
						wbRequest, results, uriConverter);
			} else {
				throw new BadQueryException("Unknown query " +
						WaybackConstants.REQUEST_TYPE);
			}

		} catch (WaybackException wbe) {

			renderer.renderException(httpRequest, httpResponse, wbRequest, wbe);

		}
	}
}
