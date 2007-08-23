/* ReplayRendererDispatcher
 *
 * $Id$
 *
 * Created on 5:23:35 PM Aug 8, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
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
public abstract class BaseReplayDispatcher implements ReplayDispatcher {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ReplayRenderer#renderException(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      org.archive.wayback.core.WaybackRequest,
	 *      org.archive.wayback.exception.WaybackException)
	 */
	public void renderException(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			WaybackException exception) throws ServletException, IOException {

		// the "standard HTML" response handler:
		String finalJspPath = errorJsp;

		// try to not cause client errors by sending the HTML response if
		// this request is ebedded, and is obviously one of the special types:
		if (requestIsEmbedded(httpRequest, wbRequest)) {

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

		dispatcher.forward(httpRequest, httpResponse);
	}

	/**
	 * @param wbRequest
	 * @param result
	 * @param resource
	 * @return the correct ReplayRenderer for the Resource
	 */
	public abstract ReplayRenderer getRenderer(WaybackRequest wbRequest,
			SearchResult result, Resource resource);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ReplayRenderer#renderResource(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      org.archive.wayback.core.WaybackRequest,
	 *      org.archive.wayback.core.SearchResult,
	 *      org.archive.wayback.core.Resource,
	 *      org.archive.wayback.ResultURIConverter,
	 *      org.archive.wayback.core.SearchResults)
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter, SearchResults results)
			throws ServletException, IOException {
		
		ReplayRenderer renderer = getRenderer(wbRequest, result, resource);
		try {
			renderer.renderResource(httpRequest, httpResponse, wbRequest, result, 
					resource, uriConverter, results);
		} catch (WaybackException e) {
			renderException(httpRequest, httpResponse, wbRequest, e);
		}
	}
}
