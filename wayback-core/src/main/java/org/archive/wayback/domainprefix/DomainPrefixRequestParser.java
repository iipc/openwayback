/* DomainPrefixRequestParser
 *
 * $Id$
 *
 * Created on 10:20:21 AM Aug 10, 2007.
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
package org.archive.wayback.domainprefix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixRequestParser extends BaseRequestParser {

	String hostPort = "localhost:8081";

	private final Pattern REPLAY_REGEX = 
		Pattern.compile("^(\\d{1,14})\\.(.*)$");
	private final Pattern QUERY_REGEX = 
		Pattern.compile("^(\\d{0,13})\\*\\.(.*)$");
	
	private String getRequestString(final String host, 
			HttpServletRequest httpRequest) {
		String path = httpRequest.getRequestURI();
		String query = httpRequest.getQueryString();

		String r = "";
		if(path == null) {
			path = "/";
		}
		if(query != null && query.length() > 0) {
			r = "http://" + host + path + "?" + query;
		} else {
			r = "http://" + host + path;						
		}
		return r;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.RequestParser#parse(javax.servlet.http.HttpServletRequest, org.archive.wayback.webapp.WaybackContext)
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint wbContext) throws BadQueryException {
		
		WaybackRequest wbRequest = null;
		String server = httpRequest.getServerName() + 
			":" + httpRequest.getServerPort();
		if(server.endsWith(hostPort)) {
			int length = server.length() - hostPort.length();
			if(server.length() > hostPort.length()) {
				String prefix = server.substring(0,length - 1);
				Matcher replayMatcher = REPLAY_REGEX.matcher(prefix);
				if (replayMatcher != null && replayMatcher.matches()) {
					wbRequest = new WaybackRequest();
					String dateStr = replayMatcher.group(1);
					String host = replayMatcher.group(2);

					String requestUrl = getRequestString(host,httpRequest);

					wbRequest.put(WaybackConstants.REQUEST_EXACT_DATE, dateStr);
					wbRequest.put(WaybackConstants.REQUEST_TYPE,
							WaybackConstants.REQUEST_REPLAY_QUERY);
					try {
						wbRequest.setRequestUrl(requestUrl);
					} catch (URIException e) {
						e.printStackTrace();
						wbRequest = null;
					}
				} else {
					Matcher queryMatcher = QUERY_REGEX.matcher(prefix);
					if(queryMatcher != null && queryMatcher.matches()) {
						wbRequest = new WaybackRequest();
						String dateStr = queryMatcher.group(1);
						String host = queryMatcher.group(2);
						String startDate;
						String endDate;
						if(dateStr.length() == 0) {
							startDate = getEarliestTimestamp();
							endDate = getLatestTimestamp();
						} else {
							startDate = Timestamp.parseBefore(dateStr).getDateStr();
							endDate = Timestamp.parseAfter(dateStr).getDateStr();
						}
						wbRequest.put(WaybackConstants.REQUEST_START_DATE,startDate);
						wbRequest.put(WaybackConstants.REQUEST_END_DATE,endDate);
						wbRequest.put(WaybackConstants.REQUEST_TYPE,
								WaybackConstants.REQUEST_URL_QUERY);

						String requestUrl = getRequestString(host,httpRequest);
						
						try {
							wbRequest.setRequestUrl(requestUrl);
						} catch (URIException e) {
							e.printStackTrace();
							wbRequest = null;
						}
					}
				}
			}
		}
		return wbRequest;
	}

	/**
	 * @return the hostPort
	 */
	public String getHostPort() {
		return hostPort;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}
}
