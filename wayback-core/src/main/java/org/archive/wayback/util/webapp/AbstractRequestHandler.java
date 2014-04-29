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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.exception.BadQueryException;


/**
 * Abstract RequestHandler implementation which performs the minimal behavior
 * for self registration with a RequestMapper, requiring subclasses to implement
 * only handleRequest(). 
 * 
 * @author brad
 *
 */
public abstract class AbstractRequestHandler implements RequestHandler {
	private String beanName = null;
	private String accessPointPath = null;
	private ServletContext servletContext = null;
	private int internalPort = 0;

	public void setBeanName(final String beanName) {
		this.beanName = beanName; 
	}
	public String getBeanName() {
		return beanName;
	}
	
	public int getInternalPort()
	{
		return internalPort;
	}
	
	public void setInternalPort(int internalPort)
	{
		this.internalPort = internalPort;
	}
	
	public String getAccessPointPath() {
		return accessPointPath;
	}

	public void setAccessPointPath(String accessPointPath) {
		this.accessPointPath = accessPointPath;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}

	// Refactor: this method is only called by RequestMapper, and eventually
	// calls RequestMapper.addRequestHandler() through static method BeanNameRegistrar.registerHandler().
	// AbstractRequestHandler does not play any active role there.  Move this code to RequestMapper.
	/**
	 * @deprecated 2014-04-24 call {@link BeanNameRegistrar#registerHandler(RequestHandler, RequestMapper)} directly.
	 */
	public void registerPortListener(RequestMapper requestMapper) {
		BeanNameRegistrar.registerHandler(this, requestMapper);
	}

	/**
	 * @deprecated 2014-04-23 use {@link RequestMapper#getRequestContextPath(HttpServletRequest)} directly.
	 */
	public String translateRequestPath(HttpServletRequest httpRequest) {
		return RequestMapper.getRequestContextPath(httpRequest);
	}

	/**
	 * @deprecated 2014-04-23 use {@link RequestMapper#getRequestContextPathQuery(HttpServletRequest)} directly
	 */
	public String translateRequestPathQuery(HttpServletRequest httpRequest) {
		return RequestMapper.getRequestContextPathQuery(httpRequest);
	}

	// Refactor: move getMapParam, getRequiredMapParam and getMapParamOrEmpty
	// to RequestParser base class.
	/**
	 * Extract the first value in the array mapped to by field in queryMap
	 * @param queryMap the Map in which to search
	 * @param field the field value desired
	 * @return the first value in the array mapped to field in queryMap, if 
	 * present, null otherwise
	 */
	public static String getMapParam(Map<String,String[]> queryMap,
			String field) {
		String arr[] = queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	/**
	 * Extract the first value in the array mapped to by field in queryMap
	 * @param queryMap the Map in which to search
	 * @param field the field value desired
	 * @return the first value in the array mapped to field in queryMap, if 
	 * present. A BadQueryException is thrown if there is no appropriate value
	 * @throws BadQueryException if there is nothing mapped to field, or if the
	 * Array mapped to field is empty
	 */
	public static String getRequiredMapParam(Map<String,String[]> queryMap,
			String field)
	throws BadQueryException {
		// TODO: Throw something different, org.archive.wayback.util should have
		// no references outside of org.archive.wayback.util 
		String value = getMapParam(queryMap,field);
		if(value == null) {
			throw new BadQueryException("missing field " + field);
		}
		if(value.length() == 0) {
			throw new BadQueryException("empty field " + field);			
		}
		return value;
	}

	/**
	 * Extract the first value in the array mapped to by field in queryMap
	 * @param map the Map in which to search
	 * @param param the field value desired
	 * @return the first value in the array mapped to field in queryMap, if 
	 * present, or an empty string otherwise
	 */
	public static String getMapParamOrEmpty(Map<String,String[]> map, 
			String param) {
		String val = getMapParam(map,param);
		return (val == null) ? "" : val;
	}
}
