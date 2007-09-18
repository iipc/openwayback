package org.archive.wayback.authenticationcontrol;

import java.util.List;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;

public class HTTPAuthBooleanOperator implements BooleanOperator<WaybackRequest> {
	private List<String> allowedUsers = null;
	public boolean isTrue(WaybackRequest value) {
		if(allowedUsers == null) {
			return false;
		}
		String currentUser = value.get(WaybackConstants.REQUEST_REMOTE_USER);
		if(currentUser == null) {
			return false;
		}
		return allowedUsers.contains(currentUser);
	}
	public List<String> getAllowedUsers() {
		return allowedUsers;
	}
	public void setAllowedUsers(List<String> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}
}
