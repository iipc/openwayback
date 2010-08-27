/* RequestMapper
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class maintains a mapping of RequestHandlers and ShutDownListeners, to
 * allow (somewhat) efficient mapping and delegation of incoming requests to
 * the appropriate RequestHandler.
 * 
 * This class uses PortMapper to delegate some of the responsibility of mapping
 * requests received on a particular port, and also allows configuration of a
 * global PRE RequestHandler, which gets first dibs on EVERY incoming request,
 * as well as a global POST RequestHandler, which may attempt to handle any
 * incoming request not handled by the normal RequestHandler mapping.
 * 
 * @author brad
 *
 */
public class RequestMapper {

	private static final Logger LOGGER = Logger.getLogger(
			RequestMapper.class.getName());
	
	private ArrayList<ShutdownListener> shutdownListeners = null;
	
	private HashMap<Integer,PortMapper> portMap = null;
	private RequestHandler globalPreRequestHandler = null;
	private RequestHandler globalPostRequestHandler = null;
	
	private final static String REQUEST_CONTEXT_PREFIX = 
		"webapp-request-context-path-prefix";
	
	/**
	 * Bean name used to register the special global PRE RequestHandler. 
	 */
	public final static String GLOBAL_PRE_REQUEST_HANDLER = "-";
	/**
	 * Bean name used to register the special global POST RequestHandler. 
	 */
	public final static String GLOBAL_POST_REQUEST_HANDLER = "+";

	/**
	 * Construct a RequestMapper, for the given RequestHandler objects, on the
	 * specified ServletContext. This method will call setServletContext() on
	 * each RequestMapper, followed immediately by registerPortListener()
	 * 
	 * @param requestHandlers Collection of RequestHandlers which handle 
	 * requests
	 * @param servletContext the webapp ServletContext where this RequestMapper
	 * is configured.
	 */
	public RequestMapper(Collection<RequestHandler> requestHandlers,
			ServletContext servletContext) {
		portMap = new HashMap<Integer, PortMapper>();
		shutdownListeners = new ArrayList<ShutdownListener>();
		Iterator<RequestHandler> itr = requestHandlers.iterator();
		LOGGER.info("Registering handlers.");
		while(itr.hasNext()) {
			RequestHandler requestHandler = itr.next();
			requestHandler.setServletContext(servletContext);
			requestHandler.registerPortListener(this);
		}
		LOGGER.info("Registering handlers complete.");
	}

	/**
	 * Request the shutdownListener object to be notified of ServletContext
	 * shutdown.
	 * @param shutdownListener the object which needs to have shutdown() called
	 * when the ServletContext is destroyed.
	 */
	public void addShutdownListener(ShutdownListener shutdownListener) {
		shutdownListeners.add(shutdownListener);
	}
	/**
	 * Configure the specified RequestHandler to handle ALL incoming requests
	 * before any other normal mapping.
	 * @param requestHandler the global PRE RequestHandler
	 */
	public void addGlobalPreRequestHandler(RequestHandler requestHandler) {
		globalPreRequestHandler = requestHandler;
	}
	/**
	 * Configure the specified RequestHandler to handle ALL incoming requests
	 * after all other normal mapping has been attempted
	 * @param requestHandler the global POST RequestHandler
	 */
	public void addGlobalPostRequestHandler(RequestHandler requestHandler) {
		globalPostRequestHandler = requestHandler;
	}
	/**
	 * Register the RequestHandler to accept requests on the given port, for the
	 * specified host and path.
	 * @param port the integer port on which the RequestHandler gets requests.
	 * @param host the String Host which the RequestHandler matches, or null, if
	 * the RequestHandler should match ALL hosts.
	 * @param path the String path which the RequestHandler matches, or null, if
	 * the RequestHandler should match ALL paths.
	 * @param requestHandler the RequestHandler to register.
	 */
	public void addRequestHandler(int port, String host, String path, 
			RequestHandler requestHandler) {
		LOGGER.info("Registered:" + port + ":" +
				(host == null ? "(null)" : host) + ":"
				+ (path == null ? "(null)" : path));
		Integer portInt = Integer.valueOf(port);
		PortMapper portMapper = portMap.get(portInt);
		if(portMapper == null) {
			portMapper = new PortMapper(portInt);
			portMap.put(portInt, portMapper);
		}
		portMapper.addRequestHandler(host, path, requestHandler);
	}

	public RequestHandlerContext mapRequest(HttpServletRequest request) {
		RequestHandlerContext handlerContext = null;
		
		int port = request.getLocalPort();
		Integer portInt = Integer.valueOf(port);
		PortMapper portMapper = portMap.get(portInt);
		if(portMapper != null) {
			handlerContext = portMapper.getRequestHandlerContext(request);
		}
		return handlerContext;
	}
	
	/**
	 * Map the incoming request to the appropriate RequestHandler, including
	 * the PRE and POST RequestHandlers, if configured.
	 * @param request the incoming HttpServletRequest
	 * @param response the HttpServletResponse to return data to the client
	 * @return true if a response was returned to the client.
	 * @throws ServletException for usual reasons.
	 * @throws IOException for usual reasons.
	 */
	public boolean handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		boolean handled = false;
		if(globalPreRequestHandler != null) {
			handled = 
				globalPreRequestHandler.handleRequest(request, response);
		}
		if(handled == false) {
			RequestHandlerContext handlerContext = mapRequest(request);
			if(handlerContext != null) {
				RequestHandler requestHandler = 
					handlerContext.getRequestHandler();
				// need to add trailing "/" iff prefix is not "/":
				String pathPrefix = handlerContext.getPathPrefix();
				if(!pathPrefix.equals("/")) {
					pathPrefix += "/";
				}
				request.setAttribute(REQUEST_CONTEXT_PREFIX,pathPrefix); 
				handled = requestHandler.handleRequest(request, response);
			}
		}
		if(handled == false) {
			if(globalPostRequestHandler != null) {
				handled = 
					globalPostRequestHandler.handleRequest(request, response);
			}
		}
		return handled;
	}

	/**
	 * notify all registered ShutdownListener objects that the ServletContext is
	 * being destroyed.
	 */
	public void shutdown() {
		for(ShutdownListener shutdownListener : shutdownListeners) {
			try {
				shutdownListener.shutdown();
			} catch(Exception e) {
				LOGGER.severe("failed shutdown"+e.getMessage());
			}
		}
	}
	
	/**
	 * Extract the request path prefix, as computed at RequestHandler mapping,
	 * from the HttpServletRequest object.
	 * 
	 * @param request HttpServlet request object being handled
	 * @return the portion of the original request path which indicated the 
	 * RequestHandler, including the trailing '/'.
	 */
	public static String getRequestPathPrefix(HttpServletRequest request) {
		return (String) request.getAttribute(REQUEST_CONTEXT_PREFIX);
	}

	/**
	 * @param request HttpServlet request object being handled
	 * @return the portion of the incoming path within the RequestHandler
	 * handling the request, not including a leading "/", and not including 
	 * query arguments.
	 */
	public static String getRequestContextPath(HttpServletRequest request) {
		String prefix = (String) request.getAttribute(REQUEST_CONTEXT_PREFIX);
		String requestUrl = request.getRequestURI();
		if(prefix == null) {
			return requestUrl;
		}
		if(requestUrl.startsWith(prefix)) {
			return requestUrl.substring(prefix.length());
		}
		return requestUrl;
	}

	/**
	 * @param request HttpServlet request object being handled
	 * @return the portion of the incoming path within the RequestHandler
	 * handling the request, not including a leading "/", including query 
	 * arguments.
	 */
	public static String getRequestContextPathQuery(HttpServletRequest request) {
		String prefix = (String) request.getAttribute(REQUEST_CONTEXT_PREFIX);
		StringBuilder sb = new StringBuilder(request.getRequestURI());
		String requestUrl = null;
		String query = request.getQueryString();
		if(query != null) {
			requestUrl = sb.append("?").append(query).toString();
		} else {
			requestUrl = sb.toString();
		}
		if(prefix == null) {
			return requestUrl;
		}
		if(requestUrl.startsWith(prefix)) {
			return requestUrl.substring(prefix.length());
		}
		return requestUrl;
	}
}
