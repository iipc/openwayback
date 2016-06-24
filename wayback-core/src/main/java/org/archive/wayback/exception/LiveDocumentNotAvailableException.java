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

	private String url;
	private int statuscode = -1;

	/**
	 * Construct exception with URL, details message and cause.
	 * @param url URL of target live resource
	 * @param message details of error causing this error
	 * @param cause an exception causing this error (can be {@code null} if not applicable)
	 */
	public LiveDocumentNotAvailableException(String url, String message, Throwable cause) {
		super(message);
		this.id = ID;
		if (cause != null)
			initCause(cause);
		this.url = url;
	}

	public LiveDocumentNotAvailableException(String url, Throwable cause) {
		this(url, "Live document unavailable", cause);
	}
	
	/**
	 * Construct exception with URL and HTTP status code
	 * @param url URL of target live resource
	 * @param code HTTP status code returned by target server
	 */
	public LiveDocumentNotAvailableException(String url, int code) {
		this(url, "Live document unavailable (HTTP " + code + " returned", null);
		this.statuscode = code;
	}

	/**
	 * Construct exception with URL and HTTP status code
	 * @param url URL of target live resource
	 * @param code HTTP status code returned by target server
	 */
	public LiveDocumentNotAvailableException(URL url, int code) {
		this(url.toString(), code);
	}

	/**
	 * Construct exception with URL and details message.
	 * @param url URL of target live resource
	 * @param message details of an error causing this exception
	 */
	public LiveDocumentNotAvailableException(URL url, String message) {
		this(url.toString(), message, null);
	}

	/**
	 * Construct exception with URL and cause.
	 * @param url URL of target live resource 
	 * @param cause an exception causing this error.
	 */
	public LiveDocumentNotAvailableException(URL url, Throwable cause) {
		this(url.toString(), "Live document unavailable", cause);
	}
	
	/**
	 * Constructor with URL only.
	 * Avoid using this constructor because details of the cause is
	 * unavailable.
	 * @param url URL of target live document
	 * @deprecated 2016-06-09 use constructor with {@code cause} or {@code message}
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
	 * exception. Note that this returns -1 if this exception is
	 * initialized through constructor without code argument. Use
	 * <code>cause</code> for failure details.
	 * Value zero is reserved for compatibility with old LiveWebCache
	 * implementations. Don't use it in the new code.
	 * @return HTTP status code, or -1 if not applicable.
	 */
	public int getOriginalStatuscode() {
		return statuscode;
	}

	/**
	 * return details message - includes.
	 * @return details message
	 */
	public String getMessage() {
		return (url != null ? url : "<unspecified url>") + ": " + super.getMessage();
	}
}
