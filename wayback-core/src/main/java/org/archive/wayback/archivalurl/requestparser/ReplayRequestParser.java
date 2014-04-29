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

import org.archive.wayback.archivalurl.ArchivalUrl;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.util.Timestamp;

/**
 * RequestParser implementation that extracts request info from a Replay
 * Archival Url path.
 * 
 * @author brad
 */
public class ReplayRequestParser extends DateUrlPathRequestParser {
//	/**
//	 * Regex which parses Archival URL replay requests into:
//	 *      timestamp, flags, & url
//	 */
//	public final static Pattern WB_REQUEST_REGEX = Pattern
//			.compile("^(\\d{1,14})(([a-z]{2}[0-9]*_)*)/(.*)$");
	public final static Pattern TIMESTAMPCTX_REGEX = Pattern.compile("(\\d{1,14})((?:[a-z]{2}[0-9]*_)*)");
	/**
	 * @param wrapped BaseRequestParser which provides general configuration
	 */
	public ReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	@Override
	protected WaybackRequest parseDateUrl(String dateStr, String urlStr) {
		Matcher matcher = TIMESTAMPCTX_REGEX.matcher(dateStr);
		if (!matcher.matches())
			return null;

		dateStr = matcher.group(1);
		String flags = matcher.group(2);
		
		// The logic of the classic WM wrt timestamp bounding:
		// if 14-digits are specified, assume min-max range boundaries
		// if less than 14 are specified, assume min-max range boundaries
		// based upon amount given (2001 => 20010101... - 20011231...)
		// AND assume the user asked for the LATEST possible date
		// within that range...

		String startDate = null;
		String endDate = null;
		if (dateStr.length() == 12) {
			// assume this is one of those old old alexa ARCs which has 
			// some 12-digit dates. Pad with "00";
			dateStr = dateStr.concat("00");
		}
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
//			startDate = getEarliestTimestamp();
//			endDate = getLatestTimestamp();
//			dateStr = Timestamp.parseAfter(dateStr).getDateStr();
		}

		WaybackRequest wbRequest = WaybackRequest.createReplayRequest(urlStr, dateStr, startDate, endDate);
		ArchivalUrl.assignFlags(wbRequest, flags);

		return wbRequest;
	}


}
