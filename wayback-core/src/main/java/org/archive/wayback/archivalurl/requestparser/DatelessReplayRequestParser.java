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

import java.net.MalformedURLException;
import java.net.URL;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;


/**
 * @author brad
 *
 */
public class DatelessReplayRequestParser extends PathRequestParser {

	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public DatelessReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	public WaybackRequest parse(String requestPath, AccessPoint accessPoint)
			throws BetterRequestException, BadQueryException {
		/*
		 *
		 * We're trying to catch requests without a datespec, in which case,
		 * we just redirect to the same request, inserting today's datespec,
		 * and then we'll let the normal redirection occur.
		 *
		 * The one tricky point is that we don't want to defeat the
		 * server-relative redirection handling, so we want to do some
		 * inspection to make sure it actually looks like an URL, and not like:
		 * 
		 *   images/foo.gif
		 *   redirect.php?blargity=blargblarg
		 * 
		 * What would be perfect is if the user supplied http:// at the front.
		 * 
		 * So, we'll assume that if we see that, we either match, or throw a 
		 * BadQueryException.
		 *   
		 */
		
		String scheme = UrlOperations.urlToScheme(requestPath);
		if(scheme == null) {
			if(requestPath.startsWith("http:/")) {
				requestPath = "http://" + requestPath.substring(6);
				scheme = "http://";
			}
		}
		if(scheme == null) {
			try {
				URL u = new URL(UrlOperations.HTTP_SCHEME + requestPath);
				// does the authority look legit?
				if(u.getUserInfo() != null) {
					throw new BadQueryException("Unable to handle URLs with user information");
				}
				if(UrlOperations.isAuthority(u.getAuthority())) {
					// ok, we're going to assume this is good:
					String nowTS = Timestamp.currentTimestamp().getDateStr();
					String newUrl = 
						accessPoint.getUriConverter().makeReplayURI(nowTS, 
								requestPath);
					throw new BetterRequestException(newUrl);
				}
			} catch(MalformedURLException e) {
				// eat it silently
			}
		} else {
			// OK, we're going to assume this is a replay request, sans timestamp,
			// ALWAYS redirect:
	
			String nowTS = Timestamp.currentTimestamp().getDateStr();
			String newUrl = 
				accessPoint.getUriConverter().makeReplayURI(nowTS, requestPath);
			throw new BetterRequestException(newUrl);
		}
		return null;
	}

}
