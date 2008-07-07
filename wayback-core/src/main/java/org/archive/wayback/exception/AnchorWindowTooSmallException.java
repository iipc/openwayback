/* AnchorWindowTooSmallException
 *
 * $Id$
 *
 * Created on 4:16:45 PM Jul 3, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AnchorWindowTooSmallException extends WaybackException {
	private static final long serialVersionUID = 1L;
	protected static final String ID = "anchorWindowTooSmall";
	public AnchorWindowTooSmallException(String message) {
		super(message,"AnchorWindow Too Small");
		id = ID;
	}
	public AnchorWindowTooSmallException(String message,String details) {
		super(message,"AnchorWindow Too Small",details);
		id = ID;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_PRECONDITION_FAILED;
	}
}
