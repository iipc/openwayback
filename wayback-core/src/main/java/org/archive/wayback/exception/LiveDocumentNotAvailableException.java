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
package org.archive.wayback.exception;

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

/**
 * An error indicating Wayback has failed to load a resource from live Web.
 * <p>Commonly thrown by user-facing live-web-proxy implementations, but also
 * used by internal robots.txt access service.</p>
 *
 * @author brad
 */
public class LiveDocumentNotAvailableException extends WaybackException {

	private static final long serialVersionUID = 1L;
	protected static final String ID = "liveDocumentNotAvailable";
	protected static final String defaultMessage = "Live document unavailable";

	private int statuscode = 0;

	/**
	 * Constructor
	 * @param url
	 * @param code
	 */
	public LiveDocumentNotAvailableException(URL url, int code) {
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)", defaultMessage);
		id = ID;
		this.statuscode = code;
	}

	/**
	 * Constructor with message and details
	 * @param url
	 * @param code
	 * @param details
	 */
	public LiveDocumentNotAvailableException(URL url, int code, String details) {
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)", defaultMessage, details);
		id = ID;
		this.statuscode = code;
	}

	/**
	 * @param url
	 */
	public LiveDocumentNotAvailableException(String url) {
		super("The URL " + url + " is not available", defaultMessage);
		id = ID;
	}

	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_BAD_GATEWAY;
	}

	/**
	 * Return the original HTTP status code that resulted in this
	 * exception.
	 * @return HTTP status code, or 0 if not applicable.
	 */
	public int getOriginalStatuscode() {
		return statuscode;
	}
}
