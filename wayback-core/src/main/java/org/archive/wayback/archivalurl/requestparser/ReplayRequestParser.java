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

import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
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
	 * Regex which parses Archival URL replay requests into:
	 *      timestamp, flags, & url
	 */
	public final static Pattern WB_REQUEST_REGEX = Pattern
			.compile("^(\\d{1,14})(([a-z]{2}[0-9]*_)*)/(.*)$");

	/**
	 * @param wrapped BaseRequestParser which provides general configuration
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
			urlStr = matcher.group(4);
			String flags = matcher.group(2);
			assignFlags(wbRequest,flags);

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

	/**
	 * @param wbRequest
	 * @param flagsStr : "js_", "", "cs_", "cs_js_"
	 */
	private void assignFlags(WaybackRequest wbRequest, String flagsStr) {
		if(flagsStr != null) {
			String[] flags = flagsStr.split(
					ArchivalUrlRequestParser.FLAG_DELIM);
			for(String flag: flags) {
				if(flag.equals(ArchivalUrlRequestParser.CSS_CONTEXT)) {
					wbRequest.setCSSContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.JS_CONTEXT)) {
					wbRequest.setJSContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.IMG_CONTEXT)) {
					wbRequest.setIMGContext(true);
				} else if(flag.startsWith(ArchivalUrlRequestParser.CHARSET_MODE)) {
					String modeString = flag.substring(
							ArchivalUrlRequestParser.CHARSET_MODE.length());
					int mode = Integer.parseInt(modeString);
					wbRequest.setCharsetMode(mode);
				}
			}
		}
	}
}
