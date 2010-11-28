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
package org.archive.wayback.memento;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser subclass which parses "timebundle/URL" and 
 * "timemap/FORMAT/URL" requests
 * 
 * @consultant Lyudmila Balakireva
 *
 */
public class TimeBundleRequestParser extends WrappedRequestParser {
	private static final Logger LOGGER = 
		Logger.getLogger(TimeBundleRequestParser.class.getName());

	String MEMENTO_BASE = "timegate";

	/**
	 * @param wrapped BaseRequestParser holding config
	 */
	public TimeBundleRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BadQueryException,
			BetterRequestException {
		
		String requestPath = accessPoint.translateRequestPathQuery(httpRequest);
		LOGGER.fine("requestpath:" + requestPath);

		if (requestPath.startsWith("timebundle")) {

			WaybackRequest wbRequest = new WaybackRequest();
			String urlStr = requestPath.substring(requestPath.indexOf("/") + 1);
			if (wbRequest.getStartTimestamp() == null) {
				wbRequest.setStartTimestamp(getEarliestTimestamp());
			}
			if (wbRequest.getEndTimestamp() == null) {
				wbRequest.setEndTimestamp(getLatestTimestamp());
			}
			wbRequest.setCaptureQueryRequest();
			wbRequest.setRequestUrl(urlStr);

			String uriPrefix = accessPoint.getConfigs().getProperty("aggregationPrefix");
			if(uriPrefix == null) {
				// TODO: this is a hack... need to clean up the whole prefix
				// configuration setup...
				uriPrefix = accessPoint.getConfigs().getProperty("Prefix");
			}

			String betterUrl = uriPrefix + "timemap/rdf/" + urlStr;
	
			throw new BetterRequestException(betterUrl, 303);
			// TODO: is it critical to return a 303 code, or will a 302 do?
			//       if so, this and ORE.jsp can be simplified by throwing a
			//       BetterRequestException here.
//			wbRequest.put("redirect", "true");
//			return wbRequest;
		}

		if (requestPath.startsWith("timemap")) {

			String urlStrplus = requestPath
					.substring(requestPath.indexOf("/") + 1);
			String format = urlStrplus.substring(0, urlStrplus.indexOf("/"));

			LOGGER.fine("format:" + format);
			String urlStr = urlStrplus.substring(urlStrplus.indexOf("/") + 1);
			LOGGER.fine("id:" + urlStr);
			WaybackRequest wbRequest = new WaybackRequest();
			if (wbRequest.getStartTimestamp() == null) {
				wbRequest.setStartTimestamp(getEarliestTimestamp());
			}
			wbRequest.setAnchorTimestamp(getLatestTimestamp());
			wbRequest.put("format", format);
			if (wbRequest.getEndTimestamp() == null) {
				wbRequest.setEndTimestamp(getLatestTimestamp());
			}
			wbRequest.setCaptureQueryRequest();
			wbRequest.setRequestUrl(urlStr);
			if (wbRequest != null) {
				wbRequest.setResultsPerPage(getMaxRecords());
			}
			return wbRequest;

		}
		return null;
	}

}
