package org.archive.wayback.rawreplayui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCRecord;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.ReplayUI;
import org.archive.wayback.RequestParser;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;
import org.archive.wayback.exception.WaybackException;

public class RawReplayUI implements ReplayUI, RequestParser {
	private final static String JSP_PATH = "replayui.jsppath";

	private final Pattern WB_REQUEST_REGEX = Pattern
			.compile("^/(\\d{1,14})/(.*)$");

	private String jspPath = null;

	public RawReplayUI() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(Properties p) throws IOException {
		this.jspPath = (String) p.get(JSP_PATH);
		if (this.jspPath == null || this.jspPath.length() <= 0) {
			throw new IllegalArgumentException("Failed to find " + JSP_PATH);
		}
	}

	public String makeReplayURI(final HttpServletRequest request,
			ResourceResult result) {
		String protocol = "http";
		String serverName = request.getServerName();
		int serverPort = request.getServerPort();
		String context = request.getContextPath();
		return protocol + "://" + serverName
				+ (serverPort == 80 ? "" : ":" + serverPort) + context + "/"
				+ result.getTimestamp().getDateStr() + "/" + result.getUrl();
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

		matcher = WB_REQUEST_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {
			wmRequest = new WMRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);
			if (!urlStr.startsWith("http://")) {
				// HACKHACK: attempt to fixup with REFERER
				int firstSlashPos = urlStr.indexOf('/');
				if (-1 != firstSlashPos) {
					String maybeHost = urlStr.substring(0, firstSlashPos);
					if (-1 == maybeHost.indexOf('.')) {
						// no . in hostname -- lets assume that this is a
						// server-relative path:
						String referer = request.getHeader("REFERER");
						if (referer != null) {
							UURI refererURI = null;
							try {
								refererURI = UURIFactory.getInstance(referer);
								UURI resolvedURI = refererURI.resolve(urlStr);
								wmRequest.setRedirectURI(resolvedURI);

								return wmRequest;
							} catch (URIException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				urlStr = "http://" + urlStr;
			}

			wmRequest.setExactDateRequest(dateStr);
			try {
				wmRequest.setExactTimestamp(Timestamp.parseBefore(dateStr));
				wmRequest.setStartTimestamp(Timestamp.earliestTimestamp());
				wmRequest.setEndTimestamp(Timestamp.latestTimestamp());
			} catch (ParseException e1) {
				e1.printStackTrace();
				return null;
			}
			wmRequest.setRetrieval();

			try {
				UURI requestURI = UURIFactory.getInstance(urlStr);
				wmRequest.setRequestURI(requestURI);
			} catch (URIException e) {
				wmRequest = null;
			}
		}
		return wmRequest;
	}

	public void handle(final WaybackLogic wayback, WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		ResourceResults results;
		ResourceIndex idx = wayback.getResourceIndex();
		ResourceStore store = wayback.getResourceStore();

		try {
			results = idx.query(wmRequest);
		} catch (IOException e) {
			showIndexNotAvailable(wmRequest, request, response, e.getMessage());
			e.printStackTrace();
			return;
		} catch (WaybackException e) {
			showWaybackException(wmRequest, request, response, e.getMessage());
			e.printStackTrace();
			return;
		}
		Resource resource;
		ResourceResult closest = null;
		if (results.isEmpty()) {
			// if (liveWeb != null) {
			// try {
			// resource = liveWeb.retrieveResource(wmRequest);
			// } catch (IOException e) {
			// replayUI.showNotInArchive(wmRequest, response);
			// return;
			// }
			// } else {
			showNotInArchive(wmRequest, request, response);
			return;
			// }
		} else {
			closest = results.getClosest(wmRequest);
			// TODO loop here looking for closest online/available version?
			// OPTIMIZ maybe assume version is here and redirect now if not
			// exactly
			// the date user requested, before retrieving it...
			try {
				resource = store.retrieveResource(closest.getARCLocation());
			} catch (IOException e) {
				showResourceNotAvailable(wmRequest, request, response, e
						.getMessage());
				return;
			}
		}
		
		// redirect to actual date if diff than request:
		if (!wmRequest.getExactDateRequest().equals(
				closest.getTimestamp().getDateStr())) {
			String newUrl = makeReplayURI(request,closest);
			response.sendRedirect(response.encodeRedirectURL(newUrl));
			return;
		}

		replayResource(wmRequest, closest, resource, request, response, results);
	}

	public void replayResource(WMRequest wmRequest, ResourceResult result,
			Resource resource, HttpServletRequest request,
			HttpServletResponse response, ResourceResults results)
			throws IOException {

		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}
		ARCRecord record = resource.getArcRecord();
		record.skipHttpHeader();
		copyRecordHttpHeader(response, record, false);
		copy(record, response.getOutputStream());
	}

	protected void copyRecordHttpHeader(HttpServletResponse response,
			ARCRecord record, boolean noLength) throws IOException {
		Header[] headers = record.getHttpHeaders();
		int code = record.getStatusCode();
		// Only return legit status codes -- don't return any minus
		// codes, etc.
		if (code <= HttpServletResponse.SC_CONTINUE) {
			String identifier = "";
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Bad status code " + code + " (" + identifier + ").");
			return;
		}
		response.setStatus(code);
		if (headers != null) {
			// Copy all headers to the response -- even date and
			// server.
			for (int i = 0; i < headers.length; i++) {
				// TODO: Special handling of encoding and date.
				String value = headers[i].getValue();
				String name = headers[i].getName();
				if (noLength) {
					if (-1 != name.indexOf("Content-Length")) {
						continue;
					}
				}
				response.setHeader(name, (value == null) ? "" : value);
			}
		}
	}

	protected void copy(InputStream is, OutputStream os) throws IOException {
		// TODO: Don't allocate everytime.
		byte[] buffer = new byte[4 * 1024];
		for (int r = -1; (r = is.read(buffer, 0, buffer.length)) != -1;) {
			os.write(buffer, 0, r);
		}
	}

	public void showNotInArchive(WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		// TODO check for javascipt type retrieval also
		if (wmRequest.isImageRetrieval()) {

			String imageUrl = jspPath + "/error_image.gif";
			RequestDispatcher dispatcher = request
					.getRequestDispatcher(imageUrl);
			dispatcher.forward(request, response);

		} else {

//			String message = wmRequest.getRequestURI().getURI() + " on "
//					+ wmRequest.getExactTimestamp().prettyDateTime()
//					+ " is Not in the Archive";

			String message = wmRequest.getRequestURI().getURI()
					+ " is not in the Archive";
			
			
			showError(message, request, response);
		}
	}

	public void showResourceNotAvailable(final WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException {

		showError("Unexpected Exception: index not available " + message,
				request, response);
	}

	public void showIndexNotAvailable(final WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException {

		showError("Unexpected Exception: index not available " + message,
				request, response);
	}

	public void showWaybackException(final WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException {

		showError("Bad request: " + message, request, response);
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
