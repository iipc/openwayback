/* UIQueryResults
 *
 * $Id$
 *
 * Created on 12:03:14 PM Nov 8, 2005.
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
package org.archive.wayback.query;

import java.text.ParseException;
import java.util.Iterator;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIQueryResults {
	private String searchUrl;

	private Timestamp startTimestamp;

	private Timestamp endTimestamp;

	private Timestamp firstResultTimestamp;

	private Timestamp lastResultTimestamp;

	private int resultCount;

	private SearchResults results;

	private ReplayResultURIConverter uriConverter;

	/**
	 * @param wmRequest
	 * @param results
	 * @param request
	 * @param replayUI
	 * @throws ParseException 
	 */
	public UIQueryResults(WaybackRequest wbRequest, SearchResults results,
			ReplayResultURIConverter uriConverter) throws ParseException {

		this.searchUrl = wbRequest.get(WaybackConstants.RESULT_URL);
		this.startTimestamp = Timestamp.parseBefore(results.
				getFilter(WaybackConstants.REQUEST_START_DATE));
		this.endTimestamp = Timestamp.parseBefore(results.getFilter(
				WaybackConstants.REQUEST_END_DATE));
		
		this.firstResultTimestamp = Timestamp.parseBefore(results
				.getFirstResultDate());
		this.lastResultTimestamp = Timestamp.parseBefore(results
				.getLastResultDate());

		this.resultCount = results.getResultCount();
		this.results = results;
		this.uriConverter = uriConverter;
	}

	/**
	 * @return Timestamp end cutoff requested by user
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @return first Timestamp in returned ResourceResults
	 */
	public Timestamp getFirstResultTimestamp() {
		return firstResultTimestamp;
	}

	/**
	 * @return last Timestamp in returned ResourceResults
	 */
	public Timestamp getLastResultTimestamp() {
		return lastResultTimestamp;
	}

	/**
	 * @return number of SearchResult objects in response
	 */
	public int getResultCount() {
		return resultCount;
	}

	/**
	 * @return URL or URL prefix requested by user
	 */
	public String getSearchUrl() {
		return searchUrl;
	}

	/**
	 * @return Timestamp start cutoff requested by user
	 */
	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @return Iterator of ResourceResults
	 */
	public Iterator resultsIterator() {
		return results.iterator();
	}

	/**
	 * @param result
	 * @return URL string that will replay the specified Resource Result.
	 */
	public String resultToReplayUrl(SearchResult result) {
		return uriConverter.makeReplayURI(result);
	}

	public String prettySearchEndDate() {
		return endTimestamp.prettyDate();
	}

	public String prettySearchStartDate() {
		return startTimestamp.prettyDate();
	}

}
