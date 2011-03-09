package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

public class RobotNotAvailableException extends AccessControlException {
	protected static final String ID = "accessWebNotAvailable";

	public RobotNotAvailableException(String message) {
		super(message);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}

}
