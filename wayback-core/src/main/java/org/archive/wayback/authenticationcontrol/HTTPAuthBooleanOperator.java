/* HTTPAuthBooleanOperator
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.authenticationcontrol;

import java.util.List;
import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;

/**
 * BooleanOperator which returns true if the user has authenticated as one of
 * a list of users with this server.
 * @author brad
 *
 */
public class HTTPAuthBooleanOperator implements BooleanOperator<WaybackRequest> {
	private List<String> allowedUsers = null;
	public boolean isTrue(WaybackRequest value) {
		if(allowedUsers == null) {
			return false;
		}
		String currentUser = getHTTPAuth(value);
		if(currentUser == null) {
			return false;
		}
		return allowedUsers.contains(currentUser);
	}
	private String decodeBasic(String authHeaderValue) {
		if(authHeaderValue != null) {
			if(authHeaderValue.startsWith("Basic ")) {
				String b64 = authHeaderValue.substring(6);
				byte[] decoded = Base64.decodeBase64(b64.getBytes());
				try {
					return new String(decoded,"utf-8");
				} catch (UnsupportedEncodingException e) {
					// really?...
					return new String(decoded);
				}
			}
		}
		return null;

	}
	private String getHTTPAuth(WaybackRequest request) {
		return decodeBasic(request.get("Authorization"));
	}
	
	/**
	 * @return the List of users that this operator matches against.
	 */
	public List<String> getAllowedUsers() {
		return allowedUsers;
	}
	/**
	 * @param allowedUsers the List of users that this operator matches against.
	 * format for values is "username:password"
	 */
	public void setAllowedUsers(List<String> allowedUsers) {
		this.allowedUsers = allowedUsers;
	}
}
