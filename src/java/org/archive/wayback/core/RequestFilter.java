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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class RequestFilter implements Filter {
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final String HANDLER_URL = "handler.url";

	private String handlerUrl = null;

	/**
	 * Constructor
	 */
	public RequestFilter() {
		super();
	}

	public void init(FilterConfig c) throws ServletException {

		handlerUrl = c.getInitParameter(HANDLER_URL);
		if ((handlerUrl == null) || (handlerUrl.length() <= 0)) {
			throw new ServletException("No config (" + HANDLER_URL + ")");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!handle(request, response)) {
			chain.doFilter(request, response);
		}
	}

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

	protected abstract WaybackRequest parseRequest(
			HttpServletRequest httpRequest);

	public void destroy() {

	}
}
