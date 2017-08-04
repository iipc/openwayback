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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * Class which allows semi-efficient translation of requests on a specific local
 * port to a RequestHandler object.
 *
 * Mapping within a port is based on the HTTP 1.1 Host field and the first
 * segment of the requested PATH, that is, whatever is after the context where
 * the wayback webapp was deployed, and before the first '/'.
 *
 * 2017-08-03 Now PortMapper is responsible for path part only. It is more like a
 * virtual host.
 *
 * @author brad
 */
public class PortMapper {
	private static final Logger LOGGER = Logger.getLogger(
			PortMapper.class.getName());

	// these fields are for logging / debugging, currently
	private String host;
	private int port = -1;

	private HashMap<String, RequestHandler> pathMap = null;

	/**
	 * @param port which this PortMapper is responsible for handling
	 */
	public PortMapper(String host, int port) {
		this.host = host;
		this.port = port;
		pathMap = new HashMap<String, RequestHandler>();
	}

	/**
	 * Register the RequestHandler to accept requests for the given host and
	 * port.
	 * @param host the HTTP 1.1 "Host" header which the RequestHandler should
	 * match. If null, the RequestHandler matches any "Host" header value.
	 * @param firstPath the first path of the GET request path which the
	 * RequestHandler should match. This is the first path AFTER the name the
	 * Wayback webapp is deployed under. If null, the RequestHandler matches
	 * all paths.
	 * @param requestHandler The RequestHandler to register.
	 */
	public void addRequestHandler(String firstPath,
			RequestHandler requestHandler) {
		String key = firstPath;
		if (pathMap.containsKey(key)) {
			LOGGER.warning("Duplicate port:path map for " + host + ":" + port +
					"/" + firstPath);
		} else {
			pathMap.put(key, requestHandler);
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("Registered requestHandler " + host + ":" + port +
						"/" + firstPath);
			}
		}
	}

	private String requestToFirstPath(HttpServletRequest request) {
		String requestPath = request.getRequestURI();
		String contextPath = request.getContextPath();
		if ((contextPath.length() > 0) && requestPath.startsWith(contextPath)) {
			requestPath = requestPath.substring(contextPath.length());
		}
		while (requestPath.startsWith("/")) {
			requestPath = requestPath.substring(1);
		}

		String firstPath = null;
		int slashIdx = requestPath.indexOf("/", 1);
		if (slashIdx == -1) {
			firstPath = requestPath;
		} else {
			firstPath = requestPath.substring(0, slashIdx);
		}
		return firstPath;
	}

	/**
	 * Attempts to locate the most strictly matching RequestHandler mapped to
	 * this port. Strictly matching means the lowest number in the following
	 * list:
	 *
	 * 1) request handler matching both HOST and PATH
	 * 2) request handler matching host, registered with an empty PATH
	 * 3) request handler matching path, registered with an empty HOST
	 * 4) request handler registered with empty HOST and PATH
	 *
	 * @param request the HttpServletRequest to be mapped to a RequestHandler
	 * @return the RequestHandlerContext, containing the RequestHandler and the
	 * prefix of the original request path that indicated the RequestHandler,
	 * or null, if no RequestHandler matches.
	 */
	public RequestHandlerContext getRequestHandlerContext(
			HttpServletRequest request) {
		String contextPath = request.getContextPath();
		StringBuilder pathPrefix = new StringBuilder(contextPath);
		pathPrefix.append("/");

		String firstPath = requestToFirstPath(request);
		RequestHandler handler;
		final String key = firstPath;
		handler = pathMap.get(key);
		if (handler != null) {
			LOGGER.fine("Mapped to RequestHandler with " + key);
			return new RequestHandlerContext(handler, pathPrefix.append(
				firstPath).toString());
		} else {
			LOGGER.finer("No mapping for " + key);
		}

		// path-segment parameter support.
		// if firstPath has ";", take string up to ";" and try again.
		// those route accepting in-segment parameters have this path
		// string in pathMap. firstPath with trailing ";" is already
		// looked up above, so don't look it up again.
		int paramSep = firstPath.indexOf(";");
		if (paramSep >= 0 && paramSep < firstPath.length() - 1) {
			final String key2 = firstPath.substring(0, paramSep + 1);
			handler = pathMap.get(key2);
			if (handler != null) {
				return new RequestHandlerContext(handler, pathPrefix.append(
					firstPath).toString());
			}
		}

		handler = pathMap.get(null);
		if (handler != null) {
			LOGGER.fine("Mapped to RequestHandler with null");
			return new RequestHandlerContext(handler, contextPath);
		}

		return null;
	}

	/**
	 * Return a set of paths defined.
	 * Path keys ending with ";" (representing path-segment parameter support)
	 * is not included.
	 * @return Set of String
	 */
	public Set<String> getPaths() {
		Set<String> paths = new HashSet<String>();
		for (String key : pathMap.keySet()) {
			if (!key.endsWith(";"))
				paths.add(key);
		}
		return paths;
	}
}
