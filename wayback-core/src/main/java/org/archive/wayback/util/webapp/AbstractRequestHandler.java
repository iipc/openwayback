/**
 * 
 */
package org.archive.wayback.util.webapp;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


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
}
