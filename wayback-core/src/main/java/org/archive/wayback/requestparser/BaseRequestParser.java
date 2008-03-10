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
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Class that implements the RequestParser interface, and also understands how
 * to:
 * 
 *      
 * This class will attempt to use the overridable parseCustom() method to 
 * create the WaybackRequest object, but if that fails (returns null), it will
 * fall back to: 

 * A) attempting to parse out an incoming OpenSearch format query
 * B) attempting to parse out any and all incoming form elements submitted as
 *      either GET or POST arguments
 *
 * This class also contains the functionality to extract HTTP header 
 * information into WaybackRequest objects, including Http auth info, referer,
 * remote IPs, etc.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class BaseRequestParser implements RequestParser {

	protected final static String QUERY_BASE = "query";

	protected final static String XQUERY_BASE = "xmlquery";

	protected final static String REPLAY_BASE = "replay";
	
	protected final static int DEFAULT_MAX_RECORDS = 10;

	protected int maxRecords = DEFAULT_MAX_RECORDS;
	
	protected String earliestTimestamp = null;
	protected String latestTimestamp = null;

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

	/* (non-Javadoc)
	 * @see org.archive.wayback.RequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public abstract WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint wbContext) throws BadQueryException;

	/**
	 * @return the maxRecords
	 */
	public int getMaxRecords() {
		return maxRecords;
	}

	/**
	 * @param maxRecords the maxRecords to set
	 */
	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}
	/**
	 * @param timestamp
	 */
	public void setEarliestTimestamp(String timestamp) {
		earliestTimestamp = Timestamp.parseBefore(timestamp).getDateStr();
	}
	/**
	 * @return
	 */
	public String getEarliestTimestamp() {
		if(earliestTimestamp != null) {
			return earliestTimestamp;
			
		}
		return Timestamp.earliestTimestamp().getDateStr();
	}

	public String getLatestTimestamp() {
		if(latestTimestamp != null) {
			return latestTimestamp;
		}
		return Timestamp.latestTimestamp().getDateStr();
	}

	public void setLatestTimestamp(String timestamp) {
		this.latestTimestamp = Timestamp.parseAfter(timestamp).getDateStr();
	}
}
