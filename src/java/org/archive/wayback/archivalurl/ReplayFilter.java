/* ReplayFilter
 *
 * $Id$
 *
 * Created on 1:08:38 PM Nov 8, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.RequestFilter;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;


/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ReplayFilter extends RequestFilter {

	/**
	 * Regex which parses Archival URL replay requests into timestamp + url
	 */
	private final Pattern WB_REQUEST_REGEX = Pattern
	.compile("^/(\\d{1,14})/(.*)$");


	/**
	 * Constructor
	 */
	public ReplayFilter() {
		super();
	}
	public WaybackRequest parseRequest(HttpServletRequest httpRequest) {
		WaybackRequest wbRequest = null;
		Matcher matcher = null;

		String queryString = httpRequest.getQueryString();
		String origRequestPath = httpRequest.getRequestURI();
		if (queryString != null) {
			origRequestPath = httpRequest.getRequestURI() + "?" + queryString;
		}
		String contextPath = httpRequest.getContextPath();
		if (!origRequestPath.startsWith(contextPath)) {
			return null;
		}
		String requestPath = origRequestPath.substring(contextPath.length());

		matcher = WB_REQUEST_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {
			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);
			if (!urlStr.startsWith("http://")) {
				urlStr = "http://" + urlStr;
			}

			// The logic of the classic WM wrt timestamp bounding:
			// if 14-digits are specified, assume min-max range boundaries
			// if less than 14 are specified, assume min-max range boundaries
			//     based upon amount given (2001 => 20010101... - 20011231...)
			//     AND assume the user asked for the LATEST possible date
			//     within that range...
			//
			//   ...don't ask me, I just work here.

			String startDate = null;
			String endDate = null;
			if(dateStr.length() == 14) {
				startDate = Timestamp.earliestTimestamp().getDateStr();
				endDate = Timestamp.currentTimestamp().getDateStr();
			} else {

				startDate = Timestamp.parseBefore(dateStr).getDateStr();
				endDate = Timestamp.parseAfter(dateStr).getDateStr();
				dateStr = endDate;
				
			}
			wbRequest.put(WaybackConstants.REQUEST_EXACT_DATE,dateStr);
			wbRequest.put(WaybackConstants.REQUEST_START_DATE,startDate);
			wbRequest.put(WaybackConstants.REQUEST_END_DATE,endDate);

			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_REPLAY_QUERY);
			
			String referer = httpRequest.getHeader("REFERER");
			if (referer == null) {
				referer = "";
			}
			wbRequest.put(WaybackConstants.REQUEST_REFERER_URL,referer);

			try {
				UURI requestURI = UURIFactory.getInstance(urlStr);
				wbRequest.put(WaybackConstants.REQUEST_URL,
						requestURI.toString());
			} catch (URIException e) {
				wbRequest = null;
			}
		}
		return wbRequest;
	}
}