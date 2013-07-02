package org.archive.wayback.memento;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.webapp.AccessPoint;

public class MementoAccessPoint extends AccessPoint {

	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.AccessPoint#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
//	@Override
//	public boolean handleRequest(HttpServletRequest httpRequest,
//			HttpServletResponse httpResponse) throws ServletException,
//			IOException {
//		boolean handled = super.handleRequest(httpRequest, httpResponse);
//		if(handled) {
//			addRelOriginalLink(httpResponse);
//		}
//		return handled;
//	}
//	public static void addRelOriginalLink(HttpServletResponse httpResponse) {
//		// TODO:
//	}
	public String getTimegatePrefix() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String getRequestUrl(HttpServletRequest httpRequest) {
		StringBuilder sb = new StringBuilder();
		sb.append(httpRequest.getScheme()).append("://");
		sb.append(httpRequest.getLocalName());
		sb.append(":").append(httpRequest.getLocalPort());
		sb.append(httpRequest.getRequestURI());
		String query = httpRequest.getQueryString();
		if(query != null) {
			sb.append("?").append(query);
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.AccessPoint#dispatchLocal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected boolean dispatchLocal(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		MementoUtils.addOrigHeader(httpResponse, getRequestUrl(httpRequest));
		return super.dispatchLocal(httpRequest, httpResponse);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.AccessPoint#handleReplay(org.archive.wayback.core.WaybackRequest, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleReplay(WaybackRequest wbRequest,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException, WaybackException {
		// add the Link header. It'll be overwritten.
		MementoUtils.addOrigHeader(httpResponse, wbRequest);
		super.handleReplay(wbRequest, httpRequest, httpResponse);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.webapp.AccessPoint#handleQuery(org.archive.wayback.core.WaybackRequest, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void handleQuery(WaybackRequest wbRequest,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws ServletException, IOException, WaybackException {
		// add the Link header. It'll be overwritten.
		MementoUtils.addOrigHeader(httpResponse, wbRequest);
		super.handleQuery(wbRequest, httpRequest, httpResponse);
	}
}
