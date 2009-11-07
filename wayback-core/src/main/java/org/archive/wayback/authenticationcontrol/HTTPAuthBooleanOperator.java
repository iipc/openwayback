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

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;

public class HTTPAuthBooleanOperator implements BooleanOperator<WaybackRequest> {
	private List<String> allowedUsers = null;
	public boolean isTrue(WaybackRequest value) {
		if(allowedUsers == null) {
			return false;
		}
		String currentUser = value.getRemoteUser();
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
