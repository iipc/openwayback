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

public class SimpleQueryUI implements QueryUI, RequestParser {
	private final static String JSP_PATH = "queryui.jsppath";

	private final static Pattern WB_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*[^*])$");

	private final static Pattern WB_PATH_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*)\\*$");

	private String jspPath = null;

	public SimpleQueryUI() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(Properties p) throws IOException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new IllegalArgumentException("Failed to find " + JSP_PATH);
		}
	}

	public WMRequest parseRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
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
				// TODO Auto-generated catch block
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
					// TODO Auto-generated catch block
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

	public void showWaybackException(WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException {

		showError("Unexpected Exception: " + message, request, response);
	}

	public void showNoMatches(WMRequest wmRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		request.setAttribute("results", wmRequest);
		String url = wmRequest.getRequestURI().getEscapedURI();
		String prettyStart = wmRequest.getStartTimestamp().prettyDateTime();
		String prettyEnd = wmRequest.getEndTimestamp().prettyDateTime();

		String message = "No matches for query " + url + " between "
				+ prettyStart + " and " + prettyEnd;
		showError(message, request, response);
	}

	public void showError(String message, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		request.setAttribute("message", message);

		proxyRequest(request, response, "ErrorResult.jsp");
	}

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
		// TODO Auto-generated method stub

	}

}
