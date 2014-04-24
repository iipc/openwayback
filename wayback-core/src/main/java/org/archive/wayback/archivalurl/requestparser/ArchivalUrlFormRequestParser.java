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

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.archivalurl.ArchivalUrl;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * @author brad
 *
 */
public class ArchivalUrlFormRequestParser extends FormRequestParser {
	/**
	 * @param wrapped BaseRequestParser to wrap
	 */
	public ArchivalUrlFormRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint accessPoint) throws BetterRequestException {
		WaybackRequest wbRequest = super.parse(httpRequest, accessPoint);
		if (wbRequest != null) {
			String replayTimestamp = wbRequest.getReplayTimestamp();
			if ((replayTimestamp == null) || replayTimestamp.length() == 0) {
				// lets call it a star query:
				// TODO: should we clone?
				wbRequest.setStartTimestamp(null);
				wbRequest.setEndTimestamp(null);
			}
			String requestPath = 
				accessPoint.translateRequestPathQuery(httpRequest);
			ArchivalUrl aUrl = new ArchivalUrl(wbRequest);
			String bestPath = aUrl.toString();
			if (accessPoint.isForceCleanQueries()) {
				if (!bestPath.equals(requestPath)) {
					String betterURI = (wbRequest.isReplayRequest() ? 
							accessPoint.getReplayPrefix() : 
								accessPoint.getQueryPrefix()) 
							+ bestPath;
					throw new BetterRequestException(betterURI);
				}
			}
		}
		return wbRequest;
	}
}
