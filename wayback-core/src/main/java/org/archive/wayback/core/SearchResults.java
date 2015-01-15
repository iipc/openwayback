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
package org.archive.wayback.core;

import java.util.HashMap;
import java.util.List;
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
	 * Expandable data bag for tuples associated with the search results, likely
	 * examples might be "total matching documents", "index of first document
	 * returned", etc.
	 */
	private HashMap<String, String> filters = null;

	/**
	 * List of URL strings that were not included in these results, but may be
	 * what the user was looking for.
	 */
	private List<String> closeMatches = null;

	private long returnedCount = -1;
	private long firstReturned = -1;
	private long matchingCount = -1;
	private long numRequested = -1;

	/**
	 * Constructor
	 */
	public SearchResults() {
		filters = new HashMap<String, String>();
	}

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
	 * @return boolean, true if key {@code key} exists in filters
	 */
	public boolean containsFilter(String key) {
		return filters.containsKey(key);
	}

	/**
	 * @param key
	 * @return value of key {@code key} in filters
	 */
	public String getFilter(String key) {
		return filters.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return previous String value of key {@code key} or null if there was none
	 */
	public String putFilter(String key, String value) {
		return filters.put(key, value);
	}

	/**
	 * @return Returns the filters.
	 */
	public Map<String, String> getFilters() {
		return filters;
	}

	private long getLongFilter(String key) {
		String tmp = getFilter(key);
		if (tmp == null) {
			return 0;
		}
		return Long.parseLong(tmp);
	}

	public long getReturnedCount() {
		if (returnedCount == -1) {
			returnedCount = getLongFilter(RESULTS_NUM_RETURNED);
		}
		return returnedCount;
	}

	public void setReturnedCount(long returnedCount) {
		this.returnedCount = returnedCount;
		putFilter(RESULTS_NUM_RETURNED, String.valueOf(returnedCount));
	}

	public long getFirstReturned() {
		if (firstReturned == -1) {
			firstReturned = getLongFilter(RESULTS_FIRST_RETURNED);
		}
		return firstReturned;
	}

	public void setFirstReturned(long firstReturned) {
		this.firstReturned = firstReturned;
		putFilter(RESULTS_FIRST_RETURNED, String.valueOf(firstReturned));
	}

	public long getMatchingCount() {
		if (matchingCount == -1) {
			matchingCount = getLongFilter(RESULTS_NUM_RESULTS);
		}
		return matchingCount;
	}

	public void setMatchingCount(long matchingCount) {
		this.matchingCount = matchingCount;
		putFilter(RESULTS_NUM_RESULTS, String.valueOf(matchingCount));
	}

	public long getNumRequested() {
		if (numRequested == -1) {
			numRequested = getLongFilter(RESULTS_REQUESTED);
		}
		return numRequested;
	}

	public void setNumRequested(long numRequested) {
		this.numRequested = numRequested;
		putFilter(RESULTS_REQUESTED, String.valueOf(numRequested));
	}

	public int getNumPages() {
		double resultsMatching = getMatchingCount();
		double resultsPerPage = getNumRequested();
		if (resultsPerPage == 0) {
			return 1;
		}
		// calculate total pages:
		int numPages = (int)Math.ceil(resultsMatching / resultsPerPage);
		return numPages;
	}

	public int getCurPageNum() {
		double resultsPerPage = getNumRequested();
		double firstResult = getFirstReturned();
		if (resultsPerPage == 0) {
			return 1;
		}
		// calculate total pages:
		int curPage = (int)Math.floor(firstResult / resultsPerPage) + 1;
		return curPage;
	}

	public List<String> getCloseMatches() {
		return closeMatches;
	}

	public void setCloseMatches(List<String> closeMatches) {
		this.closeMatches = closeMatches;
	}
}
