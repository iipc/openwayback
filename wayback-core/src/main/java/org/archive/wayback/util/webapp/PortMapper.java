/* PortMapper
 *
 * $Id$:
 *
 * Created on Mar 23, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.util.webapp;

import java.util.HashMap;
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
 * @author brad
 */
public class PortMapper {
	private static final Logger LOGGER = Logger.getLogger(
			PortMapper.class.getName());
	private int port = -1;
	private HashMap<String, RequestHandler> pathMap = null;
	/**
	 * @param port which this PortMapper is responsible for handling
	 */
	public PortMapper(int port) {
		this.port = port;
		pathMap = new HashMap<String, RequestHandler>();
	}
	private String hostPathToKey(String host, String firstPath) {
		StringBuilder sb = null;
		if((host == null) && (firstPath == null)) {
			return null;
		}
		sb = new StringBuilder();
		if(host != null) {
			sb.append(host);
		}
		sb.append("/");
		if(firstPath != null) {
			sb.append(firstPath);
		}
		return sb.toString();
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
	public void addRequestHandler(String host, String firstPath, 
			RequestHandler requestHandler) {
		String key = hostPathToKey(host,firstPath);
		if(pathMap.containsKey(key)) {
			LOGGER.warning("Duplicate port:path map for " + port +
					":" + key);
		} else {
			LOGGER.info("Registered requestHandler(port/host/path) (" +
					port + "/" + host + "/" + firstPath + "): " + key);
			pathMap.put(key,requestHandler);
		}
	}
	
	private String requestToFirstPath(HttpServletRequest request) {
		String firstPath = null;
		String requestPath = request.getRequestURI();
		String contextPath = request.getContextPath();
		if((contextPath.length() > 0) && requestPath.startsWith(contextPath)) {
			requestPath = requestPath.substring(contextPath.length());
		}
		while(requestPath.startsWith("/")) {
			requestPath = requestPath.substring(1);
		}
		
		int slashIdx = requestPath.indexOf("/",1);
		if(slashIdx == -1) {
			firstPath = requestPath;
		} else {
			firstPath = requestPath.substring(0,slashIdx);
		}
		return firstPath;
	}

	private String requestToHost(HttpServletRequest request) {
		return request.getServerName();
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
		String host = requestToHost(request);
		String contextPath = request.getContextPath();
		StringBuilder pathPrefix = new StringBuilder(contextPath);
		if(contextPath.length() == 0) {
			pathPrefix.append("/");
		}
		String firstPath = requestToFirstPath(request);
		RequestHandler handler = pathMap.get(hostPathToKey(host,firstPath));
		if(handler != null) {
			return new RequestHandlerContext(handler,
					pathPrefix.append(firstPath).toString());
		}
		handler = pathMap.get(hostPathToKey(host,null));
		if(handler != null) {
			return new RequestHandlerContext(handler,contextPath);
		}
		handler = pathMap.get(hostPathToKey(null,firstPath));
		if(handler != null) {
			return new RequestHandlerContext(handler,
					pathPrefix.append(firstPath).toString());
		}
		handler = pathMap.get(null);
		if(handler != null) {
			return new RequestHandlerContext(handler,contextPath);
		}
		return null;
	}
}
