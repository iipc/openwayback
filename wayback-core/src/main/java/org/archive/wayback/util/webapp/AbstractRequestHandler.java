/**
 * 
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
	private ServletContext servletContext = null;

	public void setBeanName(final String beanName) {
		this.beanName = beanName; 
	}
	public String getBeanName() {
		return beanName;
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	public ServletContext getServletContext() {
		return servletContext;
	}

	public void registerPortListener(RequestMapper requestMapper) {
		BeanNameRegistrar.registerHandler(this, requestMapper);
	}

	public String translateRequestPath(HttpServletRequest httpRequest) {
		return RequestMapper.getRequestContextPath(httpRequest);
	}

	public String translateRequestPathQuery(HttpServletRequest httpRequest) {
		return RequestMapper.getRequestContextPathQuery(httpRequest);
	}

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
