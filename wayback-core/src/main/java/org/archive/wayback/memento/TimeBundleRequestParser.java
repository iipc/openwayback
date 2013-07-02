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

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser subclass which parses "timebundle/URL" and redirects to the
 * configured TimeMap..
 * 
 * @consultant Lyudmila Balakireva
 *
 */
public class TimeBundleRequestParser extends WrappedRequestParser implements MementoConstants {
	private static final Logger LOGGER = 
		Logger.getLogger(TimeBundleRequestParser.class.getName());

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

		if (requestPath.startsWith(TIMEBUNDLE)) {
			String requestUrl = 
				requestPath.substring(requestPath.indexOf("/") + 1);
			String timemapUrl =
				MementoUtils.getTimemapUrl(accessPoint,FORMAT_LINK,requestUrl);

			throw new BetterRequestException(timemapUrl,
					TIMEBUNDLE_RESPONSE_CODE);
		}
		return null;
	}

}
