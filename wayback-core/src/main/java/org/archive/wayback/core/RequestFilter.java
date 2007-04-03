/* RequestFilter
 *
 * $Id$
 *
 * Created on 1:17:08 PM Nov 8, 2005.
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
package org.archive.wayback.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.exception.ConfigurationException;

/**
 * Common Servlet Filter functionality for parsing incoming URL requests
 * into WaybackRequest objects.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class RequestFilter implements Filter {
	/**
	 * name of attribute on Request Object to store filtered WaybackRequest
	 */
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	/**
	 * name of configuration for context-relative path to servlet that
	 * can handle the request, if a WaybackRequest is found in the request URL
	 */
	private static final String HANDLER_URL = "handler.url";

	/**
	 * context-relative URL to servlet that handles requests
	 */
	private String handlerUrl = null;

	/**
	 * Constructor
	 */
	public RequestFilter() {
		super();
	}
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig c) throws ServletException {

		Properties p = new Properties();
		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}
		try {
			this.init(p);
		} catch (ConfigurationException e) {
			//e.printStackTrace();
			throw new ServletException(e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!handle(request, response)) {
			chain.doFilter(request, response);
		}
	}

	/**
	 * initialize this RequestFilter based on a Properties assembled from
	 * Context and Filter init-params.
	 * @param p
	 * @throws ConfigurationException
	 */
	public void init(Properties p) throws ConfigurationException {
		PropertyConfiguration pc = new PropertyConfiguration(p);
		handlerUrl = pc.getString(HANDLER_URL);
	}

	/**
	 * @param request
	 * @param response
	 * @return boolean, true if a WaybackRequest was parsed from the URL
	 * @throws IOException
	 * @throws ServletException
	 */
	protected boolean handle(final ServletRequest request,
			final ServletResponse response) throws IOException,
			ServletException {
		if (!(request instanceof HttpServletRequest)) {
			return false;
		}
		if (!(response instanceof HttpServletResponse)) {
			return false;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		//HttpServletResponse httpResponse = (HttpServletResponse) response;
		WaybackRequest wbRequest = parseRequest(httpRequest);

		if (wbRequest == null) {
			return false;
		}

		request.setAttribute(WMREQUEST_ATTRIBUTE, wbRequest);
		RequestDispatcher dispatcher = request.getRequestDispatcher(handlerUrl);

		dispatcher.forward(request, response);

		return true;
	}

	/** attempt to extract a WaybackRequest from a request URL
	 * @param httpRequest
	 * @return WaybackRequest if successful, null otherwise
	 */
	protected abstract WaybackRequest parseRequest(
			HttpServletRequest httpRequest);

}
