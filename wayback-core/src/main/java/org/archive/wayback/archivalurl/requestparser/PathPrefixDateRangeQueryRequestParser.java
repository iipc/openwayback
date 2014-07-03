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
import org.archive.wayback.util.Timestamp;

/**
 * RequestParser implementation that extracts request info from an Archival Url
 * representing an url prefix and a date range.
 *
 * @author brad
 */
public class PathPrefixDateRangeQueryRequestParser extends DateUrlPathRequestParser {
	/**
	 * @param wrapped BaseRequestParser which provides general configuration
	 */
	public PathPrefixDateRangeQueryRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	private final static Pattern TIMESTAMP_REGEX = Pattern.compile("(\\d{1,14})-(\\d{1,14})\\*");

	@Override
	protected WaybackRequest parseDateUrl(String dateStr, String urlStr) {
		if (!urlStr.endsWith("*")) return null;
		urlStr = urlStr.substring(0, urlStr.length() - 1);

		Matcher matcher = TIMESTAMP_REGEX.matcher(dateStr);
		if (!matcher.matches())
			return null;

		String startDateStr = matcher.group(1);
		String endDateStr = matcher.group(2);
		String startDate = Timestamp.parseBefore(startDateStr).getDateStr();
		String endDate = Timestamp.parseAfter(endDateStr).getDateStr();

		return WaybackRequest.createUrlQueryRequest(urlStr, startDate, endDate);
	}
}
