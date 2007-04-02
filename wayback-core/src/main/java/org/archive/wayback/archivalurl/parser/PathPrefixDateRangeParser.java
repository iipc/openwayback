/* PathPrefixDateRangeParser
 *
 * $Id$
 *
 * Created on 3:50:00 PM Oct 24, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.archivalurl.ArchivalUrlParser;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 * ArchivalUrlParser that attempts to match .../wayback/DATE-DATE/URLPREFIX
 * queries
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathPrefixDateRangeParser implements ArchivalUrlParser {
	/**
	 * Regex which parses Archival URL queries into Start Timestamp + 
	 * End Timestamp + URL for URLs beginning with the URL prefix
	 */
	private final static Pattern WB_PATH_QUERY2_REGEX = Pattern
			.compile("^/(\\d{1,14})-(\\d{1,14})\\*/(.*)\\*$");

	/* (non-Javadoc)
	 * @see org.archive.wayback.archivalurl.ArchivalUrlParser#parse(java.lang.String)
	 */
	public WaybackRequest parse(String requestPath) {
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_PATH_QUERY2_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {

			wbRequest = new WaybackRequest();
			String startDateStr = matcher.group(1);
			String endDateStr = matcher.group(2);
			String urlStr = matcher.group(3);
			String startDate = Timestamp.parseBefore(startDateStr).getDateStr();
			String endDate = Timestamp.parseAfter(endDateStr).getDateStr();
			wbRequest.put(WaybackConstants.REQUEST_START_DATE,
					startDate);
			wbRequest.put(WaybackConstants.REQUEST_END_DATE,endDate);

			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_URL_PREFIX_QUERY);
			try {
                wbRequest.setRequestUrl(urlStr);
			} catch (URIException e) {
				wbRequest = null;
			}
		}
		return wbRequest;
	}
}