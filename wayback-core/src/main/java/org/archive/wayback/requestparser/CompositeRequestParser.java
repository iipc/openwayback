/* CompositeRequestParser
 *
 * $Id$
 *
 * Created on 4:52:13 PM Apr 24, 2007.
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

import org.archive.wayback.RequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.WaybackContext;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeRequestParser extends BaseRequestParser {
	private RequestParser[] parsers = null;
	
	/**
	 * 
	 */
	public void init() {
		parsers = getRequestParsers();
		for(int i = 0; i < parsers.length; i++) {
			parsers[i].setMaxRecords(maxRecords);
			parsers[i].setEarliestTimestamp(earliestTimestamp);
		}
	}

	
	protected RequestParser[] getRequestParsers() {
		RequestParser[] theParsers = {
				new OpenSearchRequestParser(),
				new FormRequestParser() 
				};
		return theParsers;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.RequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			WaybackContext wbContext) throws BadQueryException {

		WaybackRequest wbRequest = null;

		for(int i = 0; i < parsers.length; i++) {
			wbRequest = parsers[i].parse(httpRequest, wbContext);
			if(wbRequest != null) {
				break;
			}
		}
		return wbRequest;
	}
}
