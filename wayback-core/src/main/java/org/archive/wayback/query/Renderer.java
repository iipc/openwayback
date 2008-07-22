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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * Brain-dead simple QueryRenderer implementation, which shunts all the work off
 * to a .jsp file as defined by administrators. Also has basic logic to switch
 * to a different .jsp to format request asking for XML data. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class Renderer implements QueryRenderer {

	private String captureJsp = "/WEB-INF/query/HTMLCaptureResults.jsp";
	private String urlJsp = "/WEB-INF/query/HTMLUrlResults.jsp";
	private String xmlCaptureJsp = "/WEB-INF/query/XMLCaptureResults.jsp";
	private String xmlUrlJsp = "/WEB-INF/query/XMLUrlResults.jsp";
	
	public void renderCaptureResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		String jsp = captureJsp;
		if(wbRequest.isXMLMode()) {
			jsp = xmlCaptureJsp;
		}
		UIResults uiResults = new UIResults(wbRequest,uriConverter,results);
		uiResults.forward(httpRequest, httpResponse, jsp);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.QueryRenderer#renderUrlPrefixResults(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResults, org.archive.wayback.ResultURIConverter)
	 */
	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			UrlSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		String jsp = urlJsp;
		if(wbRequest.isXMLMode()) {
			jsp = xmlUrlJsp;
		}
		UIResults uiResults = new UIResults(wbRequest,uriConverter,results);
		uiResults.forward(httpRequest, httpResponse, jsp);
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

	/**
	 * @return the xmlCaptureJsp
	 */
	public String getXmlCaptureJsp() {
		return xmlCaptureJsp;
	}

	/**
	 * @param xmlCaptureJsp the xmlCaptureJsp to set
	 */
	public void setXmlCaptureJsp(String xmlCaptureJsp) {
		this.xmlCaptureJsp = xmlCaptureJsp;
	}
	/**
	 * @return the xmlUrlJsp
	 */
	public String getXmlUrlJsp() {
		return xmlUrlJsp;
	}

	/**
	 * @param xmlUrlJsp the xmlUrlJsp to set
	 */
	public void setXmlUrlJsp(String xmlUrlJsp) {
		this.xmlUrlJsp = xmlUrlJsp;
	}
}
