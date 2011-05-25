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
import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.webapp.AccessPoint;

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

	public WaybackRequest parse(String requestPath, AccessPoint ap) 
	throws BetterRequestException {
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_REQUEST_REGEX.matcher(requestPath);
		String urlStr = null;
		if (matcher != null && matcher.matches()) {
			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			urlStr = matcher.group(4);
			String flags = matcher.group(2);
			ArchivalUrl.assignFlags(wbRequest,flags);

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
		} else {
			// see if the remainder looks like an URL:
//			String scheme = UrlOperations.urlToScheme(requestPath);
//			if(scheme != null) {
//				// lets interpret this as a replay request missing the
//				// timestamp: use "NOW"
//				String nowTS = Timestamp.currentTimestamp().getDateStr();
//				ResultURIConverter conv = ap.getUriConverter();
//
//				String betterURI = conv.makeReplayURI(nowTS, requestPath);
//				throw new BetterRequestException(betterURI);
//			} else {
//				// not obviously an URL... see if UURI can handle it:
//				String httpUrl = UrlOperations.HTTP_SCHEME + requestPath;
//				try {
//					UURIFactory.getInstance(httpUrl);
//					// that worked. use httpUrl:
//					String nowTS = Timestamp.currentTimestamp().getDateStr();
//					ResultURIConverter conv = ap.getUriConverter();
//
//					String betterURI = conv.makeReplayURI(nowTS, requestPath);
//					throw new BetterRequestException(betterURI);
//				} catch (URIException e) {
//					// oh well. lets just fail:
//				}
//			}
		
		}
		return wbRequest;
	}


}
