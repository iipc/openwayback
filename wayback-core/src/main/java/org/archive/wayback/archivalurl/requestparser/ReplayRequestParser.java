/* ReplayRequestParser
 *
 * $Id$
 *
 * Created on 6:39:51 PM Apr 24, 2007.
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
package org.archive.wayback.archivalurl.requestparser;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.httpclient.URIException;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.PathRequestParser;

/**
 * RequestParser implementation that extracts request info from a Replay
 * Archival Url path.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ReplayRequestParser extends PathRequestParser {
	private static final Logger LOGGER = Logger.getLogger(
			ReplayRequestParser.class.getName());
	/**
	 * Regex which parses Archival URL replay requests into timestamp + url
	 */
	private final Pattern WB_REQUEST_REGEX = Pattern
			.compile("^(\\d{1,14})/(.*)$");

	public WaybackRequest parse(String requestPath) {
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_REQUEST_REGEX.matcher(requestPath);
		String urlStr = null;
		if (matcher != null && matcher.matches()) {
			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			urlStr = matcher.group(2);

			// The logic of the classic WM wrt timestamp bounding:
			// if 14-digits are specified, assume min-max range boundaries
			// if less than 14 are specified, assume min-max range boundaries
			// based upon amount given (2001 => 20010101... - 20011231...)
			// AND assume the user asked for the LATEST possible date
			// within that range...
			//
			// ...don't ask me, I just work here.

			String startDate = null;
			String endDate = null;
			if (dateStr.length() == 14) {
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
			} else {

				// classic behavior:
				// startDate = Timestamp.parseBefore(dateStr).getDateStr();
				// endDate = Timestamp.parseAfter(dateStr).getDateStr();
				// dateStr = endDate;

				// "better" behavior:
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
				dateStr = Timestamp.parseAfter(dateStr).getDateStr();

			}
			wbRequest.put(WaybackConstants.REQUEST_EXACT_DATE, dateStr);
			wbRequest.put(WaybackConstants.REQUEST_START_DATE, startDate);
			wbRequest.put(WaybackConstants.REQUEST_END_DATE, endDate);

			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_REPLAY_QUERY);

			try {
//				String wbPrefix = wbRequest.getDefaultWaybackPrefix();
//				if (urlStr.startsWith(wbPrefix)) {
//					wbRequest.setBetterRequestURI(urlStr);
//				}
				wbRequest.setRequestUrl(urlStr);
			} catch (URIException e) {
				if(urlStr != null) {
					LOGGER.severe("Failed parse of url(" + urlStr + ")");
				}
				e.printStackTrace();
				wbRequest = null;
			}
		}
		return wbRequest;
	}

}
