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
package org.archive.wayback.domainprefix;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixRequestParser extends WrappedRequestParser {

	/**
	 * @param wrapped
	 */
	public DomainPrefixRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

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
		if(server.toLowerCase().endsWith(hostPort.toLowerCase())) {
			int length = server.length() - hostPort.length();
			if(server.length() > hostPort.length()) {
				String prefix = server.substring(0,length - 1);
				Matcher replayMatcher = REPLAY_REGEX.matcher(prefix);
				if (replayMatcher != null && replayMatcher.matches()) {
					wbRequest = new WaybackRequest();
					String dateStr = replayMatcher.group(1);
					String host = replayMatcher.group(2);

					String requestUrl = getRequestString(host,httpRequest);

					wbRequest.setReplayRequest();
					wbRequest.setReplayTimestamp(dateStr);
					wbRequest.setRequestUrl(requestUrl);

				} else {
					Matcher queryMatcher = QUERY_REGEX.matcher(prefix);
					if(queryMatcher != null && queryMatcher.matches()) {
						wbRequest = new WaybackRequest();
						String dateStr = queryMatcher.group(1);
						String host = queryMatcher.group(2);

						String requestUrl = getRequestString(host,httpRequest);

						String startDate;
						String endDate;
						if(dateStr.length() == 0) {
							startDate = getEarliestTimestamp();
							endDate = getLatestTimestamp();
						} else {
							startDate = Timestamp.parseBefore(dateStr).getDateStr();
							endDate = Timestamp.parseAfter(dateStr).getDateStr();
						}
						wbRequest.setCaptureQueryRequest();
						wbRequest.setStartTimestamp(startDate);
						wbRequest.setEndTimestamp(endDate);
						wbRequest.setRequestUrl(requestUrl);
					}
					// TODO: what if it doesn't match the QUERY_REGEX?
					//       throw a BadQueryException?
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
