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
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.exception.ConfigurationException;

//import org.archive.wayback.core.WaybackRequest;
//import org.archive.wayback.exception.BadQueryException;
//import org.archive.wayback.exception.ConfigurationException;

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
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
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
		if(context != null) {
			handled = context.handleRequest(httpRequest,httpResponse);
		}
		return handled;
	}
}
