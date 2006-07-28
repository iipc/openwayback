/* QueryFilter
 *
 * $Id$
 *
 * Created on 1:22:14 PM Nov 8, 2005.
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
public class QueryFilter extends RequestFilter {
	/**
	 * Regex which parses Archival URL queries into timestamp + url for exact
	 */
	private final static Pattern WB_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*[^*])$");

	/**
	 * Regex which parses Archival URL queries into timestamp + url for prefix
	 */
	private final static Pattern WB_PATH_QUERY_REGEX = Pattern
			.compile("^/(\\d{0,13})\\*/(.*)\\*$");

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
		// TODO: add parsing and handling of page numbers and results per page
		matcher = WB_QUERY_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {

			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);

			String startDate = Timestamp.parseBefore(dateStr).getDateStr();
			String endDate = Timestamp.parseAfter(dateStr).getDateStr();
			wbRequest.put(WaybackConstants.REQUEST_START_DATE,startDate);
			wbRequest.put(WaybackConstants.REQUEST_END_DATE,endDate);
			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_URL_QUERY);

			if (!urlStr.startsWith("http://")) {
				urlStr = "http://" + urlStr;
			}

			try {
				UURI requestURI = UURIFactory.getInstance(urlStr);
				wbRequest.put(WaybackConstants.REQUEST_URL,
						requestURI.toString());
				wbRequest.fixup(httpRequest);
//				wbRequest.setRequestURI(requestURI);
			} catch (URIException e) {
				wbRequest = null;
			}
		} else {
			matcher = WB_PATH_QUERY_REGEX.matcher(requestPath);
			if (matcher != null && matcher.matches()) {

				wbRequest = new WaybackRequest();
				String dateStr = matcher.group(1);
				String urlStr = matcher.group(2);
				String startDate = Timestamp.parseBefore(dateStr).getDateStr();
				String endDate = Timestamp.parseAfter(dateStr).getDateStr();
				wbRequest.put(WaybackConstants.REQUEST_START_DATE,
						startDate);
				wbRequest.put(WaybackConstants.REQUEST_END_DATE,endDate);

				wbRequest.put(WaybackConstants.REQUEST_TYPE,
						WaybackConstants.REQUEST_URL_PREFIX_QUERY);
//				wbRequest.setPathQuery();
				if (!urlStr.startsWith("http://")) {
					urlStr = "http://" + urlStr;
				}

				try {
					UURI requestURI = UURIFactory.getInstance(urlStr);
					wbRequest.put(WaybackConstants.REQUEST_URL,requestURI.toString());
					wbRequest.fixup(httpRequest);
				} catch (URIException e) {
					wbRequest = null;
				}
			}
		}

		return wbRequest;
	}

}
