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
package org.archive.wayback;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface RequestParser {
	/**
	 * attempt to transform an incoming HttpServletRequest into a 
	 * WaybackRequest object. returns null if there is missing information.
	 * 
	 * @param httpRequest ServletHttpRequest being handled
	 * @param accessPoint AccessPoint which is attempting to parse the request 
	 * @return populated WaybackRequest object if successful, null otherwise.
	 * @throws BadQueryException if the request could match this AccessPoint,
	 *         but is malformed: invalid datespec, URL, or flags
	 * @throws BetterRequestException if the request should be redirected to
	 *         provide better user feedback (corrected URL/date in address bar)
	 */        
	public abstract WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint accessPoint) throws BadQueryException, 
			BetterRequestException;
}
