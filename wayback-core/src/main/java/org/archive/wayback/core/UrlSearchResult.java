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
package org.archive.wayback.core;

import java.util.Date;

/**
 *
 *
 * @author brad
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
		put(URL_KEY, urlKey);
	}

	public String getOriginalUrl() {
		return get(URL_ORIGINAL_URL);
	}

	public void setOriginalUrl(String originalUrl) {
		put(URL_ORIGINAL_URL, originalUrl);
	}

	public String getFirstCaptureTimestamp() {
		return get(URL_FIRST_CAPTURE_TIMESTAMP);
	}

	public Date getFirstCaptureDate() {
		if (cachedFirst == -1) {
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
		if (cachedLast == -1) {
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
		if (cachedNumCaptures == -1) {
			cachedNumCaptures = Long.parseLong(get(URL_NUM_CAPTURES));
		}
		return cachedNumCaptures;
	}

	public void setNumCaptures(long numCaptures) {
		cachedNumCaptures = numCaptures;
		put(URL_NUM_CAPTURES, String.valueOf(numCaptures));
	}

	public void setNumCaptures(String numCaptures) {
		put(URL_NUM_CAPTURES, numCaptures);
	}

	public long getNumVersions() {
		if (cachedNumVersions == -1) {
			cachedNumVersions = Long.parseLong(get(URL_NUM_VERSIONS));
		}
		return cachedNumVersions;
	}

	public void setNumVersions(long numVersions) {
		cachedNumVersions = numVersions;
		put(URL_NUM_VERSIONS, String.valueOf(numVersions));
	}

	public void setNumVersions(String numVersions) {
		put(URL_NUM_VERSIONS, numVersions);
	}
}
