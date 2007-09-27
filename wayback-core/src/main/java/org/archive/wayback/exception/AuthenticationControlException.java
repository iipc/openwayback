/* AuthenticationControlException
 *
 * $Id$
 *
 * Created on 2:48:44 PM Sep 18, 2007.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class for content inaccessible due to HTTP auth problems, or
 * user coming from the wrong network.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AuthenticationControlException extends WaybackException {
	private static final long serialVersionUID = 1L;
	protected static final String ID = "authenticationControl";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public AuthenticationControlException(String message) {
		super(message,"Authenication Problem");
		id = ID;
	}
	/** 
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public AuthenticationControlException(String message, String details) {
		super(message,"Authenication Problem",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_UNAUTHORIZED;
	}
}
