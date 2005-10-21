/* RequestFilter
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.servletglue;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.net.UURI;
import org.archive.wayback.RequestParser;
import org.archive.wayback.core.WMRequest;

/**
 * Servlet filter that first attempts to recognize a WMRequest in a
 * ServletRequest, forwarding the request to appropriate Servlet handler if a
 * request is found.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RequestFilter.class
			.getName());

	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final String REQUEST_PARSER_CLASS = "requestparser.class";

	private static final String HANDLER_URL = "handler.url";

	private String handlerUrl = null;

	private RequestParser requestParser = null;

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

		String className = c.getInitParameter(REQUEST_PARSER_CLASS);
		if ((className == null) || (className.length() <= 0)) {
			throw new ServletException("No config (" + REQUEST_PARSER_CLASS
					+ ")");
		}
		try {
			requestParser = (RequestParser) Class.forName(className)
					.newInstance();
			LOGGER.info("new " + className + " requestParser created.");

		} catch (Exception e) {
			// Convert. Add info.
			throw new ServletException("Failed making requestParser with "
					+ className + ": " + e.getMessage());
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
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		WMRequest wmRequest = requestParser.parseRequest(httpRequest);

		if (wmRequest == null) {
			return false;
		}

		// if getRedirectURI returns non-null, then the request needs a
		// redirect:
		UURI redirectURI = wmRequest.getRedirectURI();
		if (redirectURI != null) {
			String redirectURL = redirectURI.getEscapedURI();
			// response.sendRedirect(response.encodeRedirectURL(redirectURL));
			httpResponse.sendRedirect(httpResponse
					.encodeRedirectURL(redirectURL));
		} else {
			request.setAttribute(WMREQUEST_ATTRIBUTE, wmRequest);
			RequestDispatcher dispatcher = request
					.getRequestDispatcher(handlerUrl);

			dispatcher.forward(request, response);
		}

		return true;
	}

	public void destroy() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
