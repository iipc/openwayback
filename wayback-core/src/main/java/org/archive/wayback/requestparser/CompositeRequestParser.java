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
package org.archive.wayback.requestparser;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.RequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Abstract RequestParser implementation. Subclasses must implement
 * the getRequestParsers() method. This implementation provides a parse() 
 * implementation, which allows each RequestParser returned by the 
 * getRequestParsers() method an attempt at parsing the incoming request.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class CompositeRequestParser extends BaseRequestParser {
	private RequestParser[] parsers = null;
	 
	/**
	 * 
	 */
	public void init() {
		parsers = getRequestParsers();
		for(RequestParser r : parsers) {
			if(r instanceof BaseRequestParser) {
				BaseRequestParser br = (BaseRequestParser) r;
				br.setMaxRecords(getMaxRecords());
				br.setEarliestTimestamp(getEarliestTimestamp());
				br.setLatestTimestamp(getLatestTimestamp());
			}
		}
	}

	
	protected abstract RequestParser[] getRequestParsers();
	
// A basic example implementation method:

//	protected abstract RequestParser[] getRequestParsers() {
//		RequestParser[] theParsers = {
//				new OpenSearchRequestParser(this),
//				new FormRequestParser(this) 
//				};
//		return theParsers;
//	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.RequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint wbContext) throws BadQueryException, BetterRequestException {

		WaybackRequest wbRequest = null;

		for(int i = 0; i < parsers.length; i++) {
			wbRequest = parsers[i].parse(httpRequest, wbContext);
			if(wbRequest != null) {
				break;
			}
		}
		return wbRequest;
	}
}
