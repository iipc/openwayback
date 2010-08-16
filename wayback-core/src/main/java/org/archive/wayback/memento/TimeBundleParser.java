package org.archive.wayback.memento;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser subclass which parses "timebundle/URL" and 
 * "timemap/FORMAT/URL" requests
 * 
 * @author Lyudmila Balakireva
 *
 */
public class TimeBundleParser extends WrappedRequestParser {
	private static final Logger LOGGER = 
		Logger.getLogger(TimeBundleParser.class.getName());

	String MEMENTO_BASE = "timegate";

	/**
	 * @param wrapped BaseRequestParser holding config
	 */
	public TimeBundleParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BadQueryException,
			BetterRequestException {
		
		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
		LOGGER.trace("requestpath:" + requestPath);

		if (requestPath.startsWith("timebundle")) {

			WaybackRequest wbRequest = new WaybackRequest();
			String urlStr = requestPath.substring(requestPath.indexOf("/") + 1);
			if (wbRequest.getStartTimestamp() == null) {
				wbRequest.setStartTimestamp(getEarliestTimestamp());
			}
			if (wbRequest.getEndTimestamp() == null) {
				wbRequest.setEndTimestamp(getLatestTimestamp());
			}
			wbRequest.setCaptureQueryRequest();
			wbRequest.setRequestUrl(urlStr);

			// TODO: is it critical to return a 303 code, or will a 302 do?
			//       if so, this and ORE.jsp can be simplified by throwing a
			//       BetterRequestException here.
			wbRequest.put("redirect", "true");
			return wbRequest;
		}

		if (requestPath.startsWith("timemap")) {

			String urlStrplus = requestPath
					.substring(requestPath.indexOf("/") + 1);
			String format = urlStrplus.substring(0, urlStrplus.indexOf("/"));

			LOGGER.trace("format:" + format);
			String urlStr = urlStrplus.substring(urlStrplus.indexOf("/") + 1);
			LOGGER.trace("id:" + urlStr);
			WaybackRequest wbRequest = new WaybackRequest();
			if (wbRequest.getStartTimestamp() == null) {
				wbRequest.setStartTimestamp(getEarliestTimestamp());
			}
			wbRequest.setAnchorTimestamp(getLatestTimestamp());
			wbRequest.put("format", format);
			if (wbRequest.getEndTimestamp() == null) {
				wbRequest.setEndTimestamp(getLatestTimestamp());
			}
			wbRequest.setCaptureQueryRequest();
			wbRequest.setRequestUrl(urlStr);
			if (wbRequest != null) {
				wbRequest.setResultsPerPage(getMaxRecords());
			}
			return wbRequest;

		}
		return null;
	}

}
