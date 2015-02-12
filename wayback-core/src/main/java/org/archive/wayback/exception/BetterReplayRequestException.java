package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.archivalurl.ArchivalUrl;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Sub-class of {@link BetterRequestException} for instructing a redirect to
 * ArchivalUrl for the URL on different timestamp.
 * <p>
 * This class generates additional HTTP headers for Memento support.
 * </p>
 */
public class BetterReplayRequestException extends BetterRequestException {

	private static final long serialVersionUID = -873414298713087775L;

	private String targetURI;
	private String timestamp;
	private CaptureSearchResults captures;

	/**
	 * Initializes with capture information and additional information for
	 * generating navigation headers (Memento headers).
	 * @param targetURI target URI, typically capture's {@code originalUrl}
	 * @param timestamp timestamp, typically capture's  {@code cpatureTimestamp}
	 * @param results Other captures for targetURI.
	 */
	public BetterReplayRequestException(String targetURI, String timestamp,
			CaptureSearchResults captures) {
		// hacky - this is okay, because #generateResponse() does not use
		// betterURI member.
		// TODO: better to define a common superclass of BetterRequestException
		// and BetterReplayRequestException,
		// that defines abstract generateResponse() method.
		super(null);
		this.targetURI = targetURI;
		this.timestamp = timestamp;
		this.captures = captures;
	}

	/**
	 * Initializes with {@code originalUrl} and {@code captureTimestamp} from {@link CaptureSearchResult}.
	 * @param capture Capture to redirect to.
	 * @param captures Other captures for targetURI.
	 */
	public BetterReplayRequestException(CaptureSearchResult capture, CaptureSearchResults captures) {
		this(capture.getOriginalUrl(), capture.getCaptureTimestamp(), captures);
	}

	@Override
	public void generateResponse(HttpServletResponse response,
			WaybackRequest wbRequest) {
		AccessPoint accessPoint = wbRequest.getAccessPoint();
		String flags = ArchivalUrl.getFlags(wbRequest);
		// XXX there's no strong reason for URLStyle.SERVER_RELATIVE. It's been a common form because
		// replayURIPrefix is often configured with server-relative URL.
		String replayURL = accessPoint.makeReplayURI(timestamp, targetURI, flags, URLStyle.SERVER_RELATIVE);
		response.setStatus(HttpServletResponse.SC_FOUND);
		response.setHeader("Location", replayURL);

		if (wbRequest.isMementoEnabled()) {
			// both URI-G redirect response and intermediate resource response
			// MUST NOT have Memento-Datetime header.
			if (wbRequest.isMementoTimegate()) {
				// URI-G redirect response
				if (accessPoint.getMementoHandler() != null) {
					accessPoint.getMementoHandler().addTimegateHeaders(response, captures, wbRequest, true);
				} else {
					MementoUtils.addTimegateHeaders(response, captures, wbRequest, true);
				}
			} else {
				// intermediate resource that redirects to URL-M
				// should the second argument wbRequest.getRequestUrl() or this.targetURI?
				// there are different approaches in the code.
				MementoUtils.addOrigHeader(response, this.targetURI);
			}
		}
	}

}
