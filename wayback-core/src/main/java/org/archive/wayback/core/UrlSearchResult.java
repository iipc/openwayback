/* UrlSearchResult
 *
 * $Id$
 *
 * Created on 7:42:06 PM Jun 26, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.core;

import java.util.Date;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlSearchResult extends SearchResult {
	private long cachedFirst = -1;
	private long cachedLast = -1;
	private long cachedNumVersions = -1;
	private long cachedNumCaptures = -1;
	
	public final static String URL_KEY = "urlkey";
	public final static String URL_ORIGINAL_URL = "originalurl";
	public final static String URL_FIRST_CAPTURE_TIMESTAMP = "firstcapturets";
	public final static String URL_LAST_CAPTURE_TIMESTAMP = "lastcapturets";
	public final static String URL_NUM_CAPTURES = "numcaptures";
	public final static String URL_NUM_VERSIONS = "numversions";
	public String getUrlKey() {
		return get(URL_KEY);
	}
	public void setUrlKey(String urlKey) {
		put(URL_KEY,urlKey);
	}
	public String getOriginalUrl() {
		return get(URL_ORIGINAL_URL);
	}
	public void setOriginalUrl(String originalUrl) {
		put(URL_ORIGINAL_URL,originalUrl);
	}
	public String getFirstCaptureTimestamp() {
		return get(URL_FIRST_CAPTURE_TIMESTAMP);
	}
	public Date getFirstCaptureDate() {
		if(cachedFirst == -1) {
			cachedFirst = tsToDate(getFirstCaptureTimestamp()).getTime();
		}
		return new Date(cachedFirst);
	}
	public void setFirstCapture(Date date) {
		cachedFirst = date.getTime();
		put(URL_FIRST_CAPTURE_TIMESTAMP, dateToTS(date));
	}
	public void setFirstCapture(String timestamp) {
		put(URL_FIRST_CAPTURE_TIMESTAMP, timestamp);
	}
	public String getLastCaptureTimestamp() {
		return get(URL_LAST_CAPTURE_TIMESTAMP);
	}
	public Date getLastCaptureDate() {
		if(cachedLast == -1) {
			cachedLast = tsToDate(getLastCaptureTimestamp()).getTime();
		}
		return new Date(cachedLast);
	}
	public void setLastCapture(Date date) {
		cachedLast = date.getTime();
		put(URL_LAST_CAPTURE_TIMESTAMP, dateToTS(date));
	}
	public void setLastCapture(String timestamp) {
		put(URL_LAST_CAPTURE_TIMESTAMP, timestamp);
	}
	public long getNumCaptures() {
		if(cachedNumCaptures == -1) {
			cachedNumCaptures = Long.parseLong(get(URL_NUM_CAPTURES));
		}
		return cachedNumCaptures;
	}
	public void setNumCaptures(long numCaptures) {
		cachedNumCaptures = numCaptures;
		put(URL_NUM_CAPTURES,String.valueOf(numCaptures));
	}
	public void setNumCaptures(String numCaptures) {
		put(URL_NUM_CAPTURES,numCaptures);
	}
	public long getNumVersions() {
		if(cachedNumVersions == -1) {
			cachedNumVersions = Long.parseLong(get(URL_NUM_VERSIONS));
		}
		return cachedNumVersions;
	}
	public void setNumVersions(long numVersions) {
		cachedNumVersions = numVersions;
		put(URL_NUM_VERSIONS,String.valueOf(numVersions));
	}
	public void setNumVersions(String numVersions) {
		put(URL_NUM_VERSIONS,numVersions);
	}
}
