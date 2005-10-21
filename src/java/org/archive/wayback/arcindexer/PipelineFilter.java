/* PipeLineServletFilter
 *
 * Created on Oct 20, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the wayback (crawler.archive.org).
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

package org.archive.wayback.arcindexer;

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

/**
 * @author brad
 * 
 */
public class PipelineFilter implements Filter {

	private final String PIPELINE_STATUS_JSP = "pipeline.statusjsp";

	private IndexPipeline pipeline = null;

	private String pipelineStatusJsp = null;

	/**
	 * Constructor
	 */
	public PipelineFilter() {
		super();
	}

	public void init(FilterConfig c) throws ServletException {

		Properties p = new Properties();

		pipelineStatusJsp = c.getInitParameter(PIPELINE_STATUS_JSP);
		if ((pipelineStatusJsp == null) || (pipelineStatusJsp.length() <= 0)) {
			throw new ServletException("No config (" + PIPELINE_STATUS_JSP
					+ ")");
		}

		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}

		pipeline = new IndexPipeline();
		try {
			pipeline.init(p);
		} catch (IOException e) {
			throw new ServletException(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
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

		PipelineStatus status = pipeline.getStatus();

		request.setAttribute("pipelinestatus", status);
		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(pipelineStatusJsp);

		dispatcher.forward(request, response);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {

	}
}
