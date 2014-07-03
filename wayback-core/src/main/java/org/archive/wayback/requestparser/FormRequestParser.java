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
package org.archive.wayback.requestparser;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser which attempts to extract data from an HTML form, that is, from
 * HTTP GET request arguments
 *
 * @author brad
 */
public class FormRequestParser extends WrappedRequestParser {
	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public FormRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * CGI argument name for Submit button...
	 */
	private final static String SUBMIT_BUTTON = "Submit";

	/*
	 * Stuff whatever GET/POST arguments are sent up into the returned
	 * WaybackRequest object, except the Submit button argument.
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint accessPoint) throws BetterRequestException {

		WaybackRequest wbRequest = null;
		@SuppressWarnings("unchecked")
		Map<String, String[]> queryMap = httpRequest.getParameterMap();
		if (queryMap.size() > 0) {
			wbRequest = new WaybackRequest();

			String base = accessPoint.translateRequestPath(httpRequest);
			if (base.startsWith(REPLAY_BASE)) {
				wbRequest.setReplayRequest();
			} else if (base.startsWith(QUERY_BASE)) {
				wbRequest.setCaptureQueryRequest();
			} else if (base.startsWith(XQUERY_BASE)) {
				wbRequest.setCaptureQueryRequest();
				wbRequest.setXMLMode(true);

			} else {
				return null;
			}
			wbRequest.setResultsPerPage(getMaxRecords());
			Set<String> keys = queryMap.keySet();
			Iterator<String> itr = keys.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				if (key.equals(SUBMIT_BUTTON)) {
					continue;
				}
				// just jam everything else in:
				String val = AccessPoint.getMapParam(queryMap, key);
				if (key.equals(WaybackRequest.REQUEST_URL)) {
					String scheme = UrlOperations.urlToScheme(val);
					if (scheme == null) {
						val = UrlOperations.HTTP_SCHEME + val;
					}
				}
				wbRequest.put(key, val);
			}
			String partialTS = wbRequest.getReplayTimestamp();
			if (partialTS != null) {
				if (wbRequest.getStartTimestamp() == null) {
					String startTS = Timestamp.parseBefore(partialTS)
							.getDateStr();
					wbRequest.setStartTimestamp(startTS);
				}
				if (wbRequest.getEndTimestamp() == null) {
					String endTS = Timestamp.parseAfter(partialTS).getDateStr();
					wbRequest.setEndTimestamp(endTS);
				}
			} else {
				if (wbRequest.getStartTimestamp() == null) {
					wbRequest.setStartTimestamp(getEarliestTimestamp());
				}
				if (wbRequest.getEndTimestamp() == null) {
					wbRequest.setEndTimestamp(getLatestTimestamp());
				}
			}
		}
		return wbRequest;
	}
}
