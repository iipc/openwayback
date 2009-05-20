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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;

/**
 * RequestParser implementation that extracts request info from a Replay
 * Archival Url path.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ReplayRequestParser extends PathRequestParser {
	/**
	 * Regex which parses Archival URL replay requests into timestamp + url
	 */
	private final Pattern WB_REQUEST_REGEX = Pattern
			.compile("^(\\d{1,14})/(.*)$");

	/**
	 * @param wrapped
	 */
	public ReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

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

			String startDate = null;
			String endDate = null;
			if (dateStr.length() == 14) {
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
				if(endDate == null) {
					endDate = Timestamp.currentTimestamp().getDateStr();
				}
			} else {

				// classic behavior:
				startDate = Timestamp.parseBefore(dateStr).getDateStr();
				endDate = Timestamp.parseAfter(dateStr).getDateStr();
				dateStr = endDate;

				// maybe "better" behavior:
//				startDate = getEarliestTimestamp();
//				endDate = getLatestTimestamp();
//				dateStr = Timestamp.parseAfter(dateStr).getDateStr();

			}
			wbRequest.setReplayTimestamp(dateStr);
			wbRequest.setStartTimestamp(startDate);
			wbRequest.setEndTimestamp(endDate);

			wbRequest.setReplayRequest();
			wbRequest.setRequestUrl(urlStr);
		}
		return wbRequest;
	}

}
