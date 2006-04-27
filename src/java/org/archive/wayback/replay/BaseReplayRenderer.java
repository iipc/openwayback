/* BaseReplayRenderer
 *
 * $Id$
 *
 * Created on 12:35:07 PM Apr 24, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.WaybackException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class BaseReplayRenderer implements ReplayRenderer {
	private final static String JSP_PATH = "replayui.jsppath";

	protected String jspPath;

	public void init(Properties p) throws ConfigurationException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new IllegalArgumentException("Failed to find " + JSP_PATH);
		}
	}

	protected final Pattern IMAGE_REGEX = Pattern
			.compile(".*\\.(jpg|jpeg|gif|png|bmp|tiff|tif)$");

	private final String ERROR_JSP = "ErrorResult.jsp";

	private final String ERROR_JAVASCRIPT = "ErrorJavascript.jsp";

	private final String ERROR_CSS = "ErrorCSS.jsp";

	private final String RESULT_META_JSP = "ResultMeta.jsp";

	private final String ERROR_IMAGE = "error_image.gif";

	private boolean requestIsEmbedded(HttpServletRequest httpRequest,
			WaybackRequest wbRequest) {

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

	// TODO special handling for Javascript and Images: send empty image
	// or empty text file to avoid client errors
	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		String finalJspPath = jspPath + "/" + ERROR_JSP;

		// is this object embedded?
		if (requestIsEmbedded(httpRequest, wbRequest)) {
			if (requestIsJavascript(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_JAVASCRIPT;

			} else if (requestIsCSS(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_CSS;

			} else if (requestIsImage(httpRequest, wbRequest)) {

				finalJspPath = jspPath + "/" + ERROR_IMAGE;

			}
		}

		httpRequest.setAttribute("exception", exception);

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);

		dispatcher.forward(httpRequest, httpResponse);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ReplayRenderer#renderRedirect(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      org.archive.wayback.core.WaybackRequest,
	 *      org.archive.wayback.core.SearchResult,
	 *      org.archive.wayback.core.Resource,
	 *      org.archive.wayback.ResultURIConverter)
	 */
	public void renderRedirect(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		// fake out: no redirecting in Raw/Proxy mode:
		renderResource(httpRequest, httpResponse, wbRequest, result, resource,
				uriConverter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      org.archive.wayback.core.WaybackRequest,
	 *      org.archive.wayback.core.SearchResult,
	 *      org.archive.wayback.core.Resource,
	 *      org.archive.wayback.ResultURIConverter)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		String finalJspPath = jspPath + "/" + RESULT_META_JSP;

		UIReplayResult uiResult = new UIReplayResult(httpRequest, wbRequest,
				result, resource, uriConverter);
		
		httpRequest.setAttribute("ui-result", uiResult);

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);

		dispatcher.forward(httpRequest, httpResponse);
	}
}
