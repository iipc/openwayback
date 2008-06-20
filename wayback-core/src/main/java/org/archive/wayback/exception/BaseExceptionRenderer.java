/* BaseExceptionRenderer
 *
 * $Id$
 *
 * Created on 6:27:28 PM Jun 10, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.exception;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ExceptionRenderer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * Default implementation responsible for outputting error responses to users
 * for expected failure situations, for both Replay and Query requests.
 * 
 * Has logic to return errors as XML, if in query mode, and if user requested 
 * XML.
 * 
 * Has logic to render errors as CSS, Javascript, and blank images, if the
 * request is Replay mode, embedded, and of an obvious type from the request URL
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BaseExceptionRenderer implements ExceptionRenderer {
	private String xmlErrorJsp = "/jsp/XMLError.jsp";
	private String errorJsp = "/jsp/HTMLError.jsp";
	private String imageErrorJsp = "/jsp/HTMLError.jsp";
	private String javascriptErrorJsp = "/jsp/JavaScriptError.jsp";
	private String cssErrorJsp = "/jsp/CSSError.jsp";

	protected final Pattern IMAGE_REGEX = Pattern
			.compile(".*\\.(jpg|jpeg|gif|png|bmp|tiff|tif)$");

	/* ERROR HANDLING RESPONSES: */

	private boolean requestIsEmbedded(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {
		// without a wbRequest, assume it is not embedded: send back HTML
		if (wbRequest == null) {
			return false;
		}
		String referer = wbRequest.get(WaybackConstants.REQUEST_REFERER_URL);
		return (referer != null && referer.length() > 0);
	}

	private boolean requestIsImage(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {
		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		if (requestUrl == null)
			return false;
		Matcher matcher = IMAGE_REGEX.matcher(requestUrl);
		return (matcher != null && matcher.matches());
	}

	private boolean requestIsJavascript(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {

		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		return (requestUrl != null) && requestUrl.endsWith(".js");
	}

	private boolean requestIsCSS(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {

		String requestUrl = wbRequest.get(WaybackConstants.REQUEST_URL);
		return (requestUrl != null) && requestUrl.endsWith(".css");
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ExceptionRenderer#renderException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.exception.WaybackException)
	 */
	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		// the "standard HTML" response handler:
		String finalJspPath = errorJsp;

		if(wbRequest.isQueryRequest()) {

			if(wbRequest.containsKey(WaybackConstants.REQUEST_XML_DATA)) {
				finalJspPath = xmlErrorJsp;
			}

		} else if (requestIsEmbedded(httpRequest, wbRequest)) {
		
			// try to not cause client errors by sending the HTML response if
			// this request is ebedded, and is obviously one of the special 
			// types:


			if (requestIsJavascript(httpRequest, wbRequest)) {

				finalJspPath = javascriptErrorJsp;

			} else if (requestIsCSS(httpRequest, wbRequest)) {

				finalJspPath = cssErrorJsp;

			} else if (requestIsImage(httpRequest, wbRequest)) {

				finalJspPath = imageErrorJsp;

			}
		}

		httpRequest.setAttribute("exception", exception);
		UIResults uiResults = new UIResults(wbRequest);
		uiResults.storeInRequest(httpRequest, finalJspPath);

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);
		if(dispatcher == null) {
			throw new ServletException("Null dispatcher for " + finalJspPath);
		}
		dispatcher.forward(httpRequest, httpResponse);
	}

	public String getErrorJsp() {
		return errorJsp;
	}

	public void setErrorJsp(String errorJsp) {
		this.errorJsp = errorJsp;
	}

	/**
	 * @return the xmlErrorJsp
	 */
	public String getXmlErrorJsp() {
		return xmlErrorJsp;
	}

	/**
	 * @param xmlErrorJsp the xmlErrorJsp to set
	 */
	public void setXmlErrorJsp(String xmlErrorJsp) {
		this.xmlErrorJsp = xmlErrorJsp;
	}

	public String getImageErrorJsp() {
		return imageErrorJsp;
	}

	public void setImageErrorJsp(String imageErrorJsp) {
		this.imageErrorJsp = imageErrorJsp;
	}

	public String getJavascriptErrorJsp() {
		return javascriptErrorJsp;
	}

	public void setJavascriptErrorJsp(String javascriptErrorJsp) {
		this.javascriptErrorJsp = javascriptErrorJsp;
	}

	public String getCssErrorJsp() {
		return cssErrorJsp;
	}

	public void setCssErrorJsp(String cssErrorJsp) {
		this.cssErrorJsp = cssErrorJsp;
	}
}
