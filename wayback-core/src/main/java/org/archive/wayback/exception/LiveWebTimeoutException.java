package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

public class LiveWebTimeoutException extends WaybackException {

	public LiveWebTimeoutException(String message) {
		super(message);
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
