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

import javax.servlet.http.HttpServletResponse;

/**
 * Exception class for malformed user query.
 * 
 * @author Brad Tofel
 */
public class BadQueryException extends WaybackException {

	private static final long serialVersionUID = 1L;
	protected static final String ID = "badQuery";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public BadQueryException(String message) {
		super(message, "Bad Query");
		id = ID;
	}

	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public BadQueryException(String message, String details) {
		super(message, "Bad Query", details);
		id = ID;
	}

	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}
}
