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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.RequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Abstract implementation of the RequestParser interface, which provides some
 * convenience methods for accessing data in Map<String,String>'s, and also
 * allows for configuring maxRecords, and earliest and latest timestamp strings.
 * 
 * Subclasses must still implement parse().
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class BaseRequestParser implements RequestParser {

	/**
	 * String path matching a query request, by form or OpenSearch
	 */
	public final static String QUERY_BASE = "query";

	/**
	 * String path matching a query request, by form or OpenSearch, indicating
	 * user requests XML data in response
	 */
	public final static String XQUERY_BASE = "xmlquery";

	/**
	 * String path matching a replay request, by form or OpenSearch
	 */
	public final static String REPLAY_BASE = "replay";
	
	/**
	 * Default maximum number of records to assume, overridden by configuration,
	 * when not specified in the client request
	 */
	public final static int DEFAULT_MAX_RECORDS = 10;

	
	private int maxRecords = DEFAULT_MAX_RECORDS;
	private String earliestTimestamp = null;
	private String latestTimestamp = null;

	public abstract WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint wbContext) throws BadQueryException, 
			BetterRequestException;

	/**
	 * @return the maxRecords to use with this RequestParser, when not specified
	 * by the client request
	 */
	public int getMaxRecords() {
		return maxRecords;
	}

	/**
	 * @param maxRecords the maxRecords to use with this RequestParser, when not
	 * specified by the client request
	 */
	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}

	/**
	 * @param timestamp the earliest timestamp to use with this RequestParser 
	 * when none is supplied by the user request
	 */
	public void setEarliestTimestamp(String timestamp) {
		earliestTimestamp = Timestamp.parseBefore(timestamp).getDateStr();
	}

	/**
	 * @return the default earliest timestamp to use with this RequestParser
	 */
	public String getEarliestTimestamp() {
		return earliestTimestamp;
	}

	/**
	 * @return default latest timestamp for this RequestParser
	 */
	public String getLatestTimestamp() {
		return latestTimestamp;
	}

	/**
	 * @param timestamp the default latest timestamp to use with this 
	 * RequestParser
	 */
	public void setLatestTimestamp(String timestamp) {
		this.latestTimestamp = Timestamp.parseAfter(timestamp).getDateStr();
	}
}
