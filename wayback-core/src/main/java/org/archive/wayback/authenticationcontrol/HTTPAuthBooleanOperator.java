/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
