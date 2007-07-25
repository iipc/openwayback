/* EclusionFilter
 *
 * $Id: ExclusionFilter.java 1276 2006-10-17 22:21:15Z bradtofel $
 *
 * Created on 3:30:05 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which uses remote access control/exclusion service to
 * filter results.
 *
 * @author brad
 * @version $Date: 2006-10-17 15:21:15 -0700 (Tue, 17 Oct 2006) $, $Revision: 1276 $
 */
public class RemoteExclusionFilter implements ObjectFilter<SearchResult> {
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
			finalUrl.append(URLEncoder.encode("http://"+urlString,"UTF-8"));
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
	public int filterObject(SearchResult r) {
		String captureDate = r.get(WaybackConstants.RESULT_CAPTURE_DATE);
		String url = r.get(WaybackConstants.RESULT_URL);
		return isBlocked(url,captureDate) ?	FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
