/* QueryRenderer
 *
 * $Id$
 *
 * Created on 2:47:42 PM Nov 7, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.query;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Renderer implements QueryRenderer {

	private String errorJsp = "/jsp/HTMLError.jsp";
	private String xmlErrorJsp = "/jsp/XMLError.jsp";
	private String captureJsp = "/jsp/HTMLResults.jsp";
	private String urlJsp = "/jsp/HTMLResults.jsp";
	private String xmlJsp = "/jsp/XMLResults.jsp";
	
	/**
	 * @param request
	 * @param response
	 * @param jspName
	 * @throws ServletException
	 * @throws IOException
	 */
	private void proxyRequest(HttpServletRequest request,
			HttpServletResponse response, final String jspPath)
			throws ServletException, IOException {

		RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
		dispatcher.forward(request, response);
	}

	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		httpRequest.setAttribute("exception", exception);
		UIResults uiResults = new UIResults(wbRequest);
		String jsp = errorJsp;
		if(wbRequest.containsKey(WaybackConstants.REQUEST_XML_DATA)) {
			jsp = xmlErrorJsp;
		}
		uiResults.storeInRequest(httpRequest,jsp);

		proxyRequest(httpRequest,httpResponse,jsp);
	}

	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIQueryResults uiResults = new UIQueryResults(httpRequest, wbRequest,
				results, uriConverter);
		String jsp = captureJsp;
		if(wbRequest.containsKey(WaybackConstants.REQUEST_XML_DATA)) {
			jsp = xmlJsp;
		}

		uiResults.storeInRequest(httpRequest,jsp);
		proxyRequest(httpRequest, httpResponse, jsp);

	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.QueryRenderer#renderUrlPrefixResults(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResults, org.archive.wayback.ResultURIConverter)
	 */
	public void renderUrlPrefixResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIQueryResults uiResults = new UIQueryResults(httpRequest, wbRequest, 
				results, uriConverter);
		String jsp = urlJsp;
		if(wbRequest.containsKey(WaybackConstants.REQUEST_XML_DATA)) {
			jsp = xmlJsp;
		}

		uiResults.storeInRequest(httpRequest,jsp);
		proxyRequest(httpRequest, httpResponse, jsp);

	}

	/**
	 * @return the errorJsp
	 */
	public String getErrorJsp() {
		return errorJsp;
	}

	/**
	 * @param errorJsp the errorJsp to set
	 */
	public void setErrorJsp(String errorJsp) {
		this.errorJsp = errorJsp;
	}

	/**
	 * @return the captureJsp
	 */
	public String getCaptureJsp() {
		return captureJsp;
	}

	/**
	 * @param captureJsp the captureJsp to set
	 */
	public void setCaptureJsp(String captureJsp) {
		this.captureJsp = captureJsp;
	}

	/**
	 * @return the urlJsp
	 */
	public String getUrlJsp() {
		return urlJsp;
	}

	/**
	 * @param urlJsp the urlJsp to set
	 */
	public void setUrlJsp(String urlJsp) {
		this.urlJsp = urlJsp;
	}
}
