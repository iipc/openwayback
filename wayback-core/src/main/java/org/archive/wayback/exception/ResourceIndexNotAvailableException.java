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

import org.archive.wayback.ResourceIndex;

/**
 * Exception thrown from {@link ResourceIndex} implementations upon failures
 * while reading underlining index data.
 * <p>
 * As such, default status is {@code SC_SERVICE_UNAVAILABLE}.
 * </p>
 * <p>
 * TODO: should receive root-cause Exception. Code throwing this exception often
 * does {@code printStackTrace()} - not a desirable practice.
 * </p>
 *
 * @author brad
 */
public class ResourceIndexNotAvailableException extends WaybackException {

	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceIndexNotAvailable";

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceIndexNotAvailableException(String message) {
		super(message, "Index not available");
		id = ID;
	}

	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceIndexNotAvailableException(String message, String details) {
		super(message, "Index not available", details);
		id = ID;
	}

	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
