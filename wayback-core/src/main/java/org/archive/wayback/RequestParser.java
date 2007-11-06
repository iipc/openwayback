/* RequestParser
 *
 * $Id$
 *
 * Created on 6:42:13 PM Apr 23, 2007.
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
package org.archive.wayback;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface RequestParser {
	/**
	 * attempt to transform an incoming HttpServletRequest into a 
	 * WaybackRequest object. returns null if there is missing information.
	 * 
	 * @param httpRequest
	 * @param wbContext 
	 * @return populated WaybackRequest object if successful, null otherwise.
	 * @throws BadQueryException 
	 */
	public abstract WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint wbContext) throws BadQueryException;
	/**
	 * @param maxRecords
	 */
	public void setMaxRecords(int maxRecords);
	/**
	 * @param timestamp
	 */
	public void setEarliestTimestamp(String timestamp);
	/**
	 * @param timestamp
	 */
	public void setLatestTimestamp(String timestamp);
}
