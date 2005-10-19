/* SimpleQueryUI
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.simplequeryui;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.QueryUI;
import org.archive.wayback.ReplayUI;
import org.archive.wayback.RequestParser;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;
import org.archive.wayback.exception.WaybackException;

/**
 * Trivial QueryUI HTTP implementation. Basic error types are reported, and very
 * non-scalable HTML UI using dispatched JSP pages.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class SimpleQueryUI implements QueryUI, RequestParser {
	private final static String JSP_PATH = "queryui.jsppath";

	private final static Pattern WB_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*[^*])$");

	private final static Pattern WB_PATH_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*)\\*$");

	private String jspPath = null;

	/**
	 * Constructor
	 */
	public SimpleQueryUI() {
		super();
	}

	public void init(Properties p) throws IOException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new IllegalArgumentException("Failed to find " + JSP_PATH);
		}
	}

	public WMRequest parseRequest(HttpServletRequest request) {
		WMRequest wmRequest = null;
		Matcher matcher = null;

		String origRequestPath = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (!origRequestPath.startsWith(contextPath)) {
			return null;
		}
		String requestPath = origRequestPath.substring(contextPath.length());

		matcher = WB_QUERY_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {

			wmRequest = new WMRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);

			try {
				wmRequest.setStartTimestamp(Timestamp.parseBefore(dateStr));
				wmRequest.setEndTimestamp(Timestamp.parseAfter(dateStr));
			} catch (ParseException e1) {
				e1.printStackTrace();
				return null;
			}
			wmRequest.setQuery();
			if (!urlStr.startsWith("http://")) {
				urlStr = "http://" + urlStr;
			}

			try {
				UURI requestURI = UURIFactory.getInstance(urlStr);
				wmRequest.setRequestURI(requestURI);
			} catch (URIException e) {
				wmRequest = null;
			}
		} else {
			matcher = WB_PATH_QUERY_REGEX.matcher(requestPath);
			if (matcher != null && matcher.matches()) {

				wmRequest = new WMRequest();
				String dateStr = matcher.group(1);
				String urlStr = matcher.group(2);
				try {
					wmRequest.setStartTimestamp(Timestamp.parseBefore(dateStr));
					wmRequest.setEndTimestamp(Timestamp.parseAfter(dateStr));
				} catch (ParseException e1) {
					e1.printStackTrace();
					return null;
				}
				wmRequest.setPathQuery();
				if (!urlStr.startsWith("http://")) {
					urlStr = "http://" + urlStr;
				}

				try {
					UURI requestURI = UURIFactory.getInstance(urlStr);
					wmRequest.setRequestURI(requestURI);
				} catch (URIException e) {
					wmRequest = null;
				}
			}
		}

		return wmRequest;
	}

	public void handle(WaybackLogic wayback, WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		ResourceIndex idx = wayback.getResourceIndex();
		ResourceResults results;
		try {
			results = idx.query(wmRequest);
		} catch (WaybackException e1) {
			showWaybackException(wmRequest, request, response, e1.getMessage());
			e1.printStackTrace();
			return;
		}
		if (results.isEmpty()) {
			try {
				showNoMatches(wmRequest, request, response);
			} catch (ServletException e) {
				// TODO Fixxx..
				throw new IOException(e.getMessage());
			}
			return;
		}
		if (wmRequest.isQuery()) {
			showQueryResults(wayback, request, response, wmRequest, results);
		} else if (wmRequest.isPathQuery()) {
			showPathQueryResults(wayback, request, response, wmRequest, results);
		} else {
			showWaybackException(wmRequest, request, response,
					"Unknown query type error");
		}
	}

	public void showQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			WMRequest wmRequest, ResourceResults results) throws IOException,
			ServletException {

		ReplayUI replayUI = wayback.getReplayUI();
		UIResults uiResults = new UIResults(wmRequest, results, request,
				replayUI);

		request.setAttribute("ui-results", uiResults);
		proxyRequest(request, response, "QueryResults.jsp");
	}

	public void showPathQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			WMRequest wmRequest, ResourceResults results) throws IOException,
			ServletException {
		ReplayUI replayUI = wayback.getReplayUI();
		UIResults uiResults = new UIResults(wmRequest, results, request,
				replayUI);

		request.setAttribute("ui-results", uiResults);
		proxyRequest(request, response, "PathQueryResults.jsp");
	}

	public void showIndexNotAvailable(WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		showError("Unexpected Exception: Index not available", request,
				response);
	}

	/**
	 * Display a WaybackException message
	 * 
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @param message
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showWaybackException(WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException {

		showError("Unexpected Exception: " + message, request, response);
	}

	public void showNoMatches(WMRequest wmRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		request.setAttribute("results", wmRequest);
		String url = wmRequest.getRequestURI().getEscapedURI();
		String prettyStart = wmRequest.getStartTimestamp().prettyDateTime();
		String prettyEnd = wmRequest.getEndTimestamp().prettyDateTime();

		String message = "No matches for query " + url + " between "
				+ prettyStart + " and " + prettyEnd;
		showError(message, request, response);
	}

	/**
	 * Display a generic error message with simple template header+footer
	 * 
	 * @param message
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showError(String message, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		request.setAttribute("message", message);

		proxyRequest(request, response, "ErrorResult.jsp");
	}

	/**
	 * @param request
	 * @param response
	 * @param jspName
	 * @throws ServletException
	 * @throws IOException
	 */
	private void proxyRequest(HttpServletRequest request,
			HttpServletResponse response, final String jspName)
			throws ServletException, IOException {

		String finalJspPath = jspPath + "/" + jspName;

		RequestDispatcher dispatcher = request
				.getRequestDispatcher(finalJspPath);
		dispatcher.forward(request, response);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
