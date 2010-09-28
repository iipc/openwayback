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

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;

/**
 * A generic handler of HttpServletRequests. very similar to an HttpServlet, but
 * the handleRequest() method returns a boolean indicating if the RequestHandler
 * returned data to the user.
 * 
 * This interface further defines methods to facilitate automatic registration
 * when loaded as a Spring configuration, and maintains a reference to the
 * ServletContext under which it accepts incoming requests.
 * 
 * @author brad
 *
 */
public interface RequestHandler extends BeanNameAware {

	/**
	 * Possibly handle an incoming HttpServletRequest, much like a normal
	 * HttpServlet, but includes a return value.
	 * @param httpRequest the incoming HttpServletRequest
	 * @param httpResponse the HttpServletResponse to return data to the client.
	 * @return true if the RequestHandler returned a response to the client,
	 * false otherwise
	 * @throws ServletException for usual reasons.
	 * @throws IOException for usual reasons.
	 */
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException;

	/**
	 * @return the "name" property of the bean from the SpringConfiguration
	 */
	public String getBeanName();

	/**
	 * Called before registerPortListener(), to enable the registration process
	 * and subsequent handleRequest() calls to access the ServletContext, via
	 * the getServletContext() method.
	 * @param servletContext the ServletContext where the RequestHandler is
	 * registered.
	 */
	public void setServletContext(ServletContext servletContext);

	/**
	 * @return the ServletContext where the RequestHandler is registered.
	 */
	public ServletContext getServletContext();

	/**
	 * Called at webapp context initialization, to allow the RequestHandler to
	 * register itself with the RequestMapper, which will delegate request 
	 * handling to the appropriate RequestHandler.
	 * @param requestMapper the RequestMapper on which this RequestHandler 
	 * should register itself, including to register for notification of context
	 * shutdown.
	 */
	public void registerPortListener(RequestMapper requestMapper);

	/**
	 * @param httpRequest the HttpServletRequest being handled
	 * @return the portion of the original incoming request that falls within
	 * this RequestHandler, not including any query information
	 */
	public String translateRequestPath(HttpServletRequest httpRequest);

	/**
	 * @param httpRequest the HttpServletRequest being handled
	 * @return the portion of the original incoming request that falls within
	 * this RequestHandler, including any query information
	 */
	public String translateRequestPathQuery(HttpServletRequest httpRequest);
}
