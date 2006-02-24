/* ExclusionFilter
 *
 * $Id$
 *
 * Created on 11:57:36 AM Jan 25, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.cdx.filter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.archive.wayback.cdx.CDXRecord;

/**
 * Filter which uses remote access control/exclusion service to filter
 * results.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ExclusionFilter implements RecordFilter {
	private static final Logger LOGGER = Logger.getLogger(ExclusionFilter.class
			.getName());


	private static String OPERATION_ARGUMENT = "operation";
	private static String CHECK_OPERATION = "check";
	
	private static String URL_ARGUMENT = "url";
	private static String USER_AGENT_ARGUMENT = "useragent";
	private static String TIMESTAMP_ARGUMENT = "timestamp"; 
	private static String OK_CONTENT = "OK";
	
	private String urlPrefix = null;
	private String userAgent = null;
	private boolean included = false;  // flag meaning we included at least 1
	private boolean inspected = false; // flag meaning we inspected at least 1
	
	/**
	 * Constructor
	 * 
	 * @param urlPrefix String prefix for remote exclusion service
	 * @param userAgent String user agent to send to remote exclusion service
	 */
	public ExclusionFilter(final String urlPrefix, final String userAgent) {
		this.urlPrefix = urlPrefix;
		this.userAgent = userAgent;
	}
	
	/**
	 * check if *all* records inspected where blocked. (are empty results 
	 * because they were all excluded by this filter?) 
	 * 
	 * @return boolean value of true if every record inspected was blocked, 
	 * false otherwise
	 */
	public boolean blockedAll() {
		return inspected && !included;
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
			byte[] bbuffer = new byte[4 * 1024];
			StringBuffer sbuffer = new StringBuffer();
			for (int r = -1; (r = is.read(bbuffer, 0, bbuffer.length)) != -1;) {
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
		if(!blocked) {
			included = true;
		}
		return blocked;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		inspected = true;
		return isBlocked(record.url,record.captureDate) ?
				RECORD_EXCLUDE : RECORD_INCLUDE;
	}
}
