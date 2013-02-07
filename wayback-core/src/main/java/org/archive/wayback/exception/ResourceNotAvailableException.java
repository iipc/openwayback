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

import org.archive.wayback.core.CaptureSearchResults;

/**
 * Exception class for queries which matching resource is not presently
 * accessible
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceNotAvailableException extends SpecificCaptureReplayException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "resourceNotAvailable";
	protected int status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	//private CaptureSearchResults results;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceNotAvailableException(String message) {
		super(message,"Resource not available");
		id = ID;
	}
	/**
	 * Constructor with message and status
	 * 
	 * @param message
	 */
	public ResourceNotAvailableException(String message, int status) {
		super(message,"Resource not available");
		id = ID;
		this.status = status;
	}	
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceNotAvailableException(String message,String details) {
		super(message,"Resource not available",details);
		id = ID;
	}
	/**
	 * Constructor with message and details and custom error code
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceNotAvailableException(String message,String details, int status) {
		super(message,"Resource not available",details);
		id = ID;
		this.status = status;
	}
	
	public ResourceNotAvailableException(String message, String details, Exception origException) {
		super(message,"Resource not available",details);
		id = ID;
		this.origException = origException;
	}
	
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return status;
	}
//	/**
//	 * @param results
//	 */
//	public void setCaptureSearchResults(CaptureSearchResults results) {
//		this.results = results;
//	}
//	public CaptureSearchResults getCaptureSearchResults() {
//		return results;
//	}
}
