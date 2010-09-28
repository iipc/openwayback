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

/**
 * A simple composition of the RequestHandler which an HttpServletRequest was
 * mapped to, and the path prefix which indicated the RequestHandler. This 
 * allows computing the portion of the original request path within the 
 * RequestHandler.
 * 
 * @author brad
 *
 */
public class RequestHandlerContext {

	private RequestHandler handler = null;
	private String pathPrefix = null;
	
	/**
	 * Constructor
	 * @param handler the RequestHandler to which the incoming request was 
	 * mapped
	 * @param pathPrefix the leading portion of the original request path that
	 * indicated the RequestHandler
	 */
	public RequestHandlerContext(RequestHandler handler, String pathPrefix) {
		this.handler = handler;
		this.pathPrefix = pathPrefix;
	}
	/**
	 * @return the RequestHandler to which the incoming request was mapped.
	 */
	public RequestHandler getRequestHandler() {
		return handler;
	}
	/**
	 * @return the leading portion of the original request path that
	 * indicated the RequestHandler
	 */
	public String getPathPrefix() {
		return pathPrefix;
	}
}
