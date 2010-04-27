/* BaseRequestParser
 *
 * $Id$
 *
 * Created on 3:15:12 PM Apr 24, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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

	protected static String getMapParam(Map<String,String[]> queryMap,
			String field) {
		String arr[] = queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	protected static String getRequiredMapParam(Map<String,String[]> queryMap,
			String field)
	throws BadQueryException {
		String value = getMapParam(queryMap,field);
		if(value == null) {
			throw new BadQueryException("missing field " + field);
		}
		if(value.length() == 0) {
			throw new BadQueryException("empty field " + field);			
		}
		return value;
	}

	protected static String getMapParamOrEmpty(Map<String,String[]> map, 
			String param) {
		String val = getMapParam(map,param);
		return (val == null) ? "" : val;
	}

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
