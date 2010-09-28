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
package org.archive.wayback.accesscontrol.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.logging.Logger;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 * SearchResultFilter which uses remote access control/exclusion service to
 * filter results.
 *
 * @deprecated superseded by ExclusionOracle
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteExclusionFilter extends ExclusionFilter {
	private static final Logger LOGGER = Logger.getLogger(RemoteExclusionFilter.class
			.getName());


	private final static int BYTE_BUFFER_SIZE = 4096;
	
	private static String OPERATION_ARGUMENT = "operation";
	private static String CHECK_OPERATION = "check";
	
	private static String URL_ARGUMENT = "url";
	private static String USER_AGENT_ARGUMENT = "useragent";
	private static String TIMESTAMP_ARGUMENT = "timestamp"; 
	private static String OK_CONTENT = "OK";
	
	private String urlPrefix = null;
	private String userAgent = null;

	// allocate byte buffer once
	private byte[] bbuffer = new byte[BYTE_BUFFER_SIZE];
	
	/**
	 * Constructor
	 * 
	 * @param urlPrefix String prefix for remote exclusion service
	 * @param userAgent String user agent to send to remote exclusion service
	 */
	public RemoteExclusionFilter(final String urlPrefix, final String userAgent) {
		this.urlPrefix = urlPrefix;
		this.userAgent = userAgent;
	}
	
	private boolean isBlocked(final String urlString, final String captureDate) {
		boolean blocked = true;
		
		StringBuffer finalUrl = new StringBuffer(urlPrefix);
		finalUrl.append("?");

		finalUrl.append(OPERATION_ARGUMENT);
		finalUrl.append("=");
		finalUrl.append(CHECK_OPERATION);


		finalUrl.append("&");
		finalUrl.append(URL_ARGUMENT);
		finalUrl.append("=");
		try {
			finalUrl.append(URLEncoder.encode(urlString,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO what happenned here?
			e.printStackTrace();
		}

		finalUrl.append("&");
		finalUrl.append(USER_AGENT_ARGUMENT);
		finalUrl.append("=");
		try {
			finalUrl.append(URLEncoder.encode(userAgent,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO what happenned here?
			e.printStackTrace();
		}

		finalUrl.append("&");
		finalUrl.append(TIMESTAMP_ARGUMENT);
		finalUrl.append("=");
		try {
			finalUrl.append(URLEncoder.encode(captureDate,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO what happenned here?
			e.printStackTrace();
		}
		
		try {

			// TODO: this is so ugly -- there must be a better/easy way.
			URL url = new URL(finalUrl.toString());
			InputStream is = url.openStream();
			// slurp the whole thing into RAM:
			// TODO: character encoding detection & handling!!
			StringBuilder sbuffer = new StringBuilder(BYTE_BUFFER_SIZE);
			for (int r = -1; (r = is.read(bbuffer, 0, BYTE_BUFFER_SIZE)) != -1;) {
				sbuffer.append(new String(bbuffer, 0, r));
			}
			String content = sbuffer.toString();
			if(content.equals(OK_CONTENT)) {
				blocked = false;
			} else {
				LOGGER.info("Disallowing " + urlString + " because " + content);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return blocked;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		String captureDate = r.getCaptureTimestamp();
		String url = r.getOriginalUrl();
		return isBlocked(url,captureDate) ?	FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
