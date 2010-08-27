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
package org.archive.wayback.util.webapp;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Top-Level integration point between a series of RequestHandler mappings and
 * a generic ServletContext. This filter is assumed to be responsible for 
 * matching ALL requests received by the webapp ("*") and uses a RequestMapper
 * to delegate incoming HttpServletRequests to the appropriate RequestHandler, 
 * via the doFilter() method.
 *
 * @author brad
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RequestFilter.class
			.getName());
	private RequestMapper mapper = null;
	private final static String CONFIG_PATH = "config-path";
	
	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();

		String configPath = servletContext.getInitParameter(CONFIG_PATH);
		if(configPath == null) {
			throw new ServletException("Missing " + CONFIG_PATH 
					+ " parameter");
		}
		String resolvedPath = servletContext.getRealPath(configPath);

		LOGGER.info("Initializing Spring config at: " +  resolvedPath);
		mapper = SpringReader.readSpringConfig(resolvedPath,servletContext);
		LOGGER.info("Initialized Spring config at: " +  resolvedPath);
	}

	public void destroy() {
		LOGGER.info("Shutdown starting.");
		mapper.shutdown();
		LOGGER.info("Shutdown complete.");
	}

	public void doFilter(ServletRequest request, ServletResponse response, 
			FilterChain chain) throws IOException, ServletException {
		boolean handled = false;
		
		if(request instanceof HttpServletRequest) {
			if(response instanceof HttpServletResponse) {
				handled = mapper.handleRequest((HttpServletRequest) request,
						(HttpServletResponse) response);
			}
		}
		if(!handled) {
			chain.doFilter(request,response);
		}
	}
}
