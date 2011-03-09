package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

public class RobotTimedOutAccessControlException extends AccessControlException {
	protected static final String ID = "accessRobotTimeout";

	/**
	 * @param message
	 */
	public RobotTimedOutAccessControlException(String message) {
		super("Robot.txt timed out",message);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
