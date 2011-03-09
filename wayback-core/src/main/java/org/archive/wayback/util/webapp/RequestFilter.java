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
package org.archive.wayback.util.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.LogManager;
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
 * Top-Level integration point between a series of RequestHandler mappings and a
 * generic ServletContext. This filter is assumed to be responsible for matching
 * ALL requests received by the webapp ("*") and uses a RequestMapper to
 * delegate incoming HttpServletRequests to the appropriate RequestHandler, via
 * the doFilter() method.
 * 
 * @author brad
 */
public class RequestFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RequestFilter.class
			.getName());
	private RequestMapper mapper = null;
	private final static String CONFIG_PATH = "config-path";
	private final static String LOGGING_CONFIG_PATH = "logging-config-path";

	public void init(FilterConfig config) throws ServletException {
		ServletContext servletContext = config.getServletContext();

		String logConfigPath = servletContext
				.getInitParameter(LOGGING_CONFIG_PATH);
		if (logConfigPath != null) {
			String resolvedLogPath = servletContext.getRealPath(logConfigPath);
			File logConfigFile = new File(resolvedLogPath);
			if (logConfigFile.exists()) {
				FileInputStream finp = null;
				try {
					finp = new FileInputStream(logConfigFile);
					LogManager.getLogManager().readConfiguration(finp);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (finp != null) {
							finp.close();
						}
					} catch (IOException e) {
						throw new ServletException(e);
					}
				}
			}
		}

		String configPath = servletContext.getInitParameter(CONFIG_PATH);
		if (configPath == null) {
			throw new ServletException("Missing " + CONFIG_PATH + " parameter");
		}
		String resolvedPath = servletContext.getRealPath(configPath);

		LOGGER.info("Initializing Spring config at: " + resolvedPath);
		mapper = SpringReader.readSpringConfig(resolvedPath, servletContext);
		LOGGER.info("Initialized Spring config at: " + resolvedPath);
	}

	public void destroy() {
		LOGGER.info("Shutdown starting.");
		mapper.shutdown();
		LOGGER.info("Shutdown complete.");
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		boolean handled = false;
		String origThreadName = Thread.currentThread().getName();
		try {
			if (request instanceof HttpServletRequest) {
				if (response instanceof HttpServletResponse) {
					handled = mapper.handleRequest((HttpServletRequest) request,
							(HttpServletResponse) response);
				}
			}
		} finally {
			Thread.currentThread().setName(origThreadName);
		}
		if (!handled) {
			chain.doFilter(request, response);
		}
	}
}
