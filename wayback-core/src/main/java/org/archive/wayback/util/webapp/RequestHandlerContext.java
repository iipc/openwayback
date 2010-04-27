/* RequestHandlerContext
 *
 * $Id$:
 *
 * Created on Apr 26, 2010.
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
