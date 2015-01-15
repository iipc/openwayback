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
 */
public class Renderer implements QueryRenderer {

	private String captureJsp = "/WEB-INF/query/HTMLCaptureResults.jsp";
	private String urlJsp = "/WEB-INF/query/HTMLUrlResults.jsp";
	private String xmlCaptureJsp = "/WEB-INF/query/XMLCaptureResults.jsp";
	private String xmlUrlJsp = "/WEB-INF/query/XMLUrlResults.jsp";

	protected ResultURIConverter queryUriConverter = null;

	public void renderCaptureResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIResults uiResults = new UIResults(wbRequest,
			(queryUriConverter != null ? queryUriConverter : uriConverter),
			results);
		if (wbRequest.isXMLMode()) {
			uiResults.forward(httpRequest, httpResponse, xmlCaptureJsp);
		} else {
			uiResults.forward(httpRequest, httpResponse, captureJsp);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.archive.wayback.QueryRenderer#renderUrlPrefixResults(javax.servlet
	 * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * org.archive.wayback.core.WaybackRequest,
	 * org.archive.wayback.core.SearchResults,
	 * org.archive.wayback.ResultURIConverter)
	 */
	public void renderUrlResults(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			UrlSearchResults results, ResultURIConverter uriConverter)
			throws ServletException, IOException {

		UIResults uiResults = new UIResults(wbRequest,
			(queryUriConverter != null ? queryUriConverter : uriConverter),
			results);
		if (wbRequest.isXMLMode()) {
			uiResults.forward(httpRequest, httpResponse, xmlUrlJsp);
		} else {
			uiResults.forward(httpRequest, httpResponse, urlJsp);
		}
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

	public ResultURIConverter getQueryUriConverter() {
		return queryUriConverter;
	}

	public void setQueryUriConverter(ResultURIConverter queryUriConverter) {
		this.queryUriConverter = queryUriConverter;
	}
}
