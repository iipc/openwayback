/* AccessControlException
 *
 * $Id$
 *
 * Created on 5:37:01 PM Jan 25, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
 * Exception class for content blocked by robots.txt, etc.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AccessControlException extends WaybackException {
	private static final long serialVersionUID = 1L;
	protected static final String ID = "accessControl";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public AccessControlException(String message) {
		super(message,"Access Problem");
		id = ID;
	}
	/** 
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public AccessControlException(String message, String details) {
		super(message,"Access Problem",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
