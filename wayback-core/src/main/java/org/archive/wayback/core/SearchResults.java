/* SearchResults
 *
 * $Id$
 *
 * Created on 12:52:13 PM Nov 9, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class SearchResults {
	/**
	 * Results: int total number of records matching, not all necc. returned.
	 */
	public static final String RESULTS_NUM_RESULTS = "numresults";
	
	/**
	 * Results: int first record of all matching returned, 1-based 
	 */
	public static final String RESULTS_FIRST_RETURNED = "firstreturned";

	/**
	 * Results: int total number of records *returned* in results 
	 */
	public static final String RESULTS_NUM_RETURNED = "numreturned";
	
	/**
	 * Results: int number of results requested
	 */
	public static final String RESULTS_REQUESTED = "resultsrequested";
	/**
	 * Expandable data bag for tuples associated with the search results, 
	 * likely examples might be "total matching documents", "index of first 
	 * document returned", etc. 
	 */
	private HashMap<String,String> filters = null;
	/**
	 * Constructor
	 */
	public SearchResults() {
		filters = new HashMap<String,String>();
	}
	private long returnedCount = -1;
	private long firstReturned = -1;
	private long matchingCount = -1;
	private long numRequested = -1;

	/**
	 * Results: indicates SearchResult objects within the SearchResults are of
	 * type UrlSearchResults.
	 */
	public static final String RESULTS_TYPE_URL = "resultstypeurl";

	/**
	 * Results: indicates SearchResult objects within the SearchResults are of
	 * type CaptureSearchResults.
	 */
	public static final String RESULTS_TYPE_CAPTURE = "resultstypecapture";

	/**
	 * Results: type of results: "Capture" or "Url"
	 */
	public static final String RESULTS_TYPE = "resultstype";
	
	/**
	 * @param key
	 * @return boolean, true if key 'key' exists in filters
	 */
	public boolean containsFilter(String key) {
		return filters.containsKey(key);
	}

	/**
	 * @param key
	 * @return value of key 'key' in filters
	 */
	public String getFilter(String key) {
		return filters.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return previous String value of key 'key' or null if there was none
	 */
	public String putFilter(String key, String value) {
		return (String) filters.put(key, value);
	}
	/**
	 * @return Returns the filters.
	 */
	public Map<String,String> getFilters() {
		return filters;
	}
	private long getLongFilter(String key) {
		String tmp = getFilter(key);
		if(tmp == null) {
			return 0;
		}
		return Long.parseLong(tmp);
	}

	public long getReturnedCount() {
		if(returnedCount == -1) {
			returnedCount = getLongFilter(RESULTS_NUM_RETURNED);
		}
		return returnedCount;
	}
	public void setReturnedCount(long returnedCount) {
		this.returnedCount = returnedCount;
		putFilter(RESULTS_NUM_RETURNED, String.valueOf(returnedCount));
	}

	public long getFirstReturned() {
		if(firstReturned == -1) {
			firstReturned = getLongFilter(RESULTS_FIRST_RETURNED);
		}
		return firstReturned;
	}

	public void setFirstReturned(long firstReturned) {
		this.firstReturned = firstReturned;
		putFilter(RESULTS_FIRST_RETURNED, String.valueOf(firstReturned));
	}

	public long getMatchingCount() {
		if(matchingCount == -1) {
			matchingCount = getLongFilter(RESULTS_NUM_RESULTS);
		}
		return matchingCount;
	}

	public void setMatchingCount(long matchingCount) {
		this.matchingCount = matchingCount;
		putFilter(RESULTS_NUM_RESULTS, String.valueOf(matchingCount));
	}

	public long getNumRequested() {
		if(numRequested == -1) {
			numRequested = getLongFilter(RESULTS_REQUESTED);
		}
		return numRequested;
	}

	public void setNumRequested(long numRequested) {
		this.numRequested = numRequested;
		putFilter(RESULTS_REQUESTED, String.valueOf(numRequested));
	}
}
