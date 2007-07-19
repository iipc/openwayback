/* RequestMapper
 *
 * $Id$
 *
 * Created on 5:36:36 PM Apr 20, 2007.
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

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.exception.ConfigurationException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * RequestMapper accepts a request, and maps that request to
 * a WaybackContext suitable for that request.
 * 
 * This object is a singleton, and the class provides methods for constructing
 * and accessing the singleton.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RequestMapper {
	private static final Logger LOGGER = Logger.getLogger(RequestMapper.class
			.getName());
	
	private final static String PORT_SEPARATOR = ":";
	

	private final static String CONFIG_PATH = "config-path";
//	private WaybackContext defaultContext = null;
//	private ServletContext servletContext = null;
	
	private XmlBeanFactory factory = null;
	/**
	 * @param configPath
	 * @param servletContext
	 * @throws ConfigurationException 
	 */
	public RequestMapper(ServletContext servletContext) throws ConfigurationException {
		
//		this.servletContext = servletContext;
		String configPath = servletContext.getInitParameter(CONFIG_PATH);
		if(configPath == null) {
			throw new ConfigurationException("Missing " + CONFIG_PATH 
					+ " parameter");
		}
		String resolvedPath = servletContext.getRealPath(configPath);
		Resource resource = new FileSystemResource(resolvedPath);
		factory = new XmlBeanFactory(resource);
		factory.preInstantiateSingletons();
	}
	
	private String getContextID(HttpServletRequest request) {
		String requestPath = request.getRequestURI();
//		String absolutePath = servletContext.getRealPath(requestPath);
//		File tmpFile = new File(absolutePath);
//		if(tmpFile.exists()) {
//			return null;
//		}
		String collection = "";
		if(requestPath.startsWith("/")) {
			int secondSlash = requestPath.indexOf("/",1);
			if(secondSlash != -1) {
				collection = PORT_SEPARATOR + 
					requestPath.substring(1,requestPath.indexOf("/",1));
			}
		}
		return String.valueOf(request.getLocalPort()) + collection;
	}
	
	/**
	 * @param request
	 * @return WaybackContext that handles the specific incoming HTTP request
	 */
	public WaybackContext mapContext(HttpServletRequest request) {

		WaybackContext context = null;
		String contextID = String.valueOf(request.getLocalPort());
		if(factory.containsBean(contextID)) {
			context = (WaybackContext) factory.getBean(contextID);
		} else {
			contextID = getContextID(request);
			if(factory.containsBean(contextID)) {
				context = (WaybackContext) factory.getBean(contextID);
			}
		}
		return context;
	}

	/**
	 * clean up all WaybackContexts, which should release resources gracefully.
	 */
	public void destroy() {
		LOGGER.info("shutting down contexts...");
		//TODO: shut everything down
	}
}
