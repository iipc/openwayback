/* RequestHandler
 *
 * $Id$
 *
 * Created on 4:24:06 PM Apr 20, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-webapp.
 *
 * wayback-webapp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-webapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-webapp; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.webapp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RequestFilter.class
			.getName());
	private RequestMapper mapper = null;
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {

		LOGGER.info("Wayback Filter initializing...");
		try {
			mapper = new RequestMapper(config.getServletContext());
		} catch (ConfigurationException e) {
			throw new ServletException(e.getMessage());
		}
		LOGGER.info("Wayback Filter initialization complete.");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		LOGGER.info("Wayback Filter de-initialization starting...");
		mapper.destroy();
		LOGGER.info("Wayback Filter de-initialization complete.");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, 
			FilterChain chain) throws IOException, ServletException {
		boolean handled = false;
		
		if(request instanceof HttpServletRequest) {
			if(response instanceof HttpServletResponse) {
				handled = handle((HttpServletRequest) request,
							(HttpServletResponse) response);
			}
		}
		if(!handled) {
			chain.doFilter(request,response);
		}
	}
	
	protected boolean handle(HttpServletRequest httpRequest, 
			HttpServletResponse httpResponse) 
	throws IOException, ServletException {

		boolean handled = false;
		RequestContext context = mapper.mapContext(httpRequest);
		if(context == null) {
			try {
			handled = 
				handleServerRelativeArchivalRedirect(httpRequest, httpResponse);
			} catch(URIException e) {
				// TODO: Log this?
				handled = false;
			}
		} else {
			handled = context.handleRequest(httpRequest,httpResponse);
		}
		return handled;
	}
	
	private boolean handleServerRelativeArchivalRedirect(
			HttpServletRequest httpRequest,	HttpServletResponse httpResponse)
	throws IOException {

		boolean handled = false;
		// hope that it's a server relative request, with a valid referrer:
		String referer = httpRequest.getHeader("Referer");
		if(referer != null) {
			UURI uri = UURIFactory.getInstance(referer);
			String path = uri.getPath();
			int secondSlash = path.indexOf('/',1);
			if(secondSlash > -1) {
				String collection = path.substring(0,secondSlash);
				String remainder = path.substring(secondSlash+1);
				int thirdSlash = remainder.indexOf('/');
				if(thirdSlash > -1) {
					String datespec = remainder.substring(0,thirdSlash);
					String url = ArchiveUtils.addImpliedHttpIfNecessary(
							remainder.substring(thirdSlash+1));
					String thisPath = httpRequest.getRequestURI();
					String queryString = httpRequest.getQueryString();
					if (queryString != null) {
						thisPath += "?" + queryString;
					}

					String resolved = UrlOperations.resolveUrl(url, thisPath);
					String contextPath = httpRequest.getContextPath();
					String finalUrl = uri.getScheme() + "://" + 
						uri.getAuthority() + contextPath + collection + "/" 
						+ datespec + "/" + resolved;
					// cross your fingers!!!
					LOGGER.info("Server-Relative-Redirect:\t" + referer + "\t" 
							+ thisPath + "\t" + finalUrl);

					// Gotta make sure this is properly cached, or
					// weird things happen:
					httpResponse.addHeader("Vary", "Referer");
					httpResponse.sendRedirect(finalUrl);
					handled = true;

				}
			}
		}
		
		return handled;
		
	}
}
