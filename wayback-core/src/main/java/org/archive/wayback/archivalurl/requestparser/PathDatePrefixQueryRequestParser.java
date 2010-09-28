/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.archivalurl.requestparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser implementation that extracts request info from an Archival Url
 * representing an exact url and a date prefix.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathDatePrefixQueryRequestParser extends PathRequestParser {
	/**
	 * @param wrapped BaseRequestParser which provides general configuration
	 */
	public PathDatePrefixQueryRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * Regex which parses Archival URL queries into timestamp + url for an exact 
	 * URL
	 */
	private final static Pattern WB_QUERY_REGEX = Pattern
			.compile("^(\\d{0,14})\\*/(.*[^*])$");

	public WaybackRequest parse(String requestPath, AccessPoint ap) {
		
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_QUERY_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {

			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);

			String startDate;
			String endDate;
			String requestDate;
			if(dateStr.length() == 0) {
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
				requestDate = endDate;
			} else if(dateStr.length() == 14) {
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
				requestDate = Timestamp.parseAfter(dateStr).getDateStr();
			} else {
				startDate = Timestamp.parseBefore(dateStr).getDateStr();
				endDate = Timestamp.parseAfter(dateStr).getDateStr();
				requestDate = endDate;
			}
			wbRequest.setStartTimestamp(startDate);
			wbRequest.setEndTimestamp(endDate);
			wbRequest.setReplayTimestamp(requestDate);
			wbRequest.setCaptureQueryRequest();
            wbRequest.setRequestUrl(urlStr);
		}
		return wbRequest;
	}
}
