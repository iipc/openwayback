/* BadContentException
 *
 * $Id$
 *
 * Created on 6:10:25 PM Aug 17, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BadContentException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "badContent";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public BadContentException(String message) {
		super(message,"Bad Content");
		id = ID;
	}
	/** 
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public BadContentException(String message, String details) {
		super(message,"Bad Content",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
	}
}
