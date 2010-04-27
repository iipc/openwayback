/* PathRequestParser
 *
 * $Id$
 *
 * Created on 6:47:21 PM Apr 24, 2007.
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
package org.archive.wayback.requestparser;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Subclass of RequestParser that acquires key request information from the
 * path component within the handling AccessPoint. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class PathRequestParser extends WrappedRequestParser {

	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public PathRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * attempt to transform an incoming HttpServletRequest into a 
	 * WaybackRequest object. returns null if there is missing information.
	 * 
	 * @param requestPath the AccessPoint relative path as received by the 
	 * 		   AccessPoint
	 * @param accessPoint AccessPoint which is attempting to parse the request 
	 * @return populated WaybackRequest object if successful, null otherwise.
	 * @throws BadQueryException if the request could match this AccessPoint,
	 *         but is malformed: invalid datespec, URL, or flags
	 * @throws BetterRequestException if the request should be redirected to
	 *         provide better user feedback (corrected URL/date in address bar)
	 */        
	public abstract WaybackRequest parse(String requestPath, 
			AccessPoint accessPoint) throws BetterRequestException, 
			BadQueryException;

	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) 
		throws BadQueryException, BetterRequestException {

		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
		WaybackRequest wbRequest = parse(requestPath, accessPoint);
		if(wbRequest != null) {
			wbRequest.setResultsPerPage(getMaxRecords());
		}

		return wbRequest;
	}
}
