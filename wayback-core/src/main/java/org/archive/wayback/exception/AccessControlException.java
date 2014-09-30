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
 * Exception indicating no captures can be replayed because
 * entire URL is blocked by robots.txt or other type of exclusion
 * rules.
 * <p>This exception will not be thrown for individual captures.
 * Per-capture exclusion will just make such captures invisible,
 * and no exception will be throwsn (unless there's no captures
 * at all as a result, in which case {@link ResourceNotInArchiveException}
 * will be thrown).</p>
 *
 * @author brad
 */
public class AccessControlException extends WaybackException {
	private static final long serialVersionUID = 1L;
	protected static final String ID = "accessControl";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public AccessControlException(String message) {
		super(message, "Access Problem");
		id = ID;
	}

	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public AccessControlException(String message, String details) {
		super(message, "Access Problem", details);
		id = ID;
	}

	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
