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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * Abstraction class to provide a single, (hopefully) simpler interface to
 * query results for UI JSPs. Handles slight massaging of SearchResults (to
 * fix zero-based/one-based numbers) URI fixup and creation for result pages,
 * and later may provide more functionality for dividing results into columns,
 * again to simplify UI JSPs.  
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIQueryResults extends UIResults {
	
	private String searchUrl;

	private Timestamp startTimestamp;

	private Timestamp endTimestamp;

	private Timestamp exactRequestedTimestamp;

	private long resultsReturned;
	private long resultsMatching;
	private long resultsPerPage;
	private long firstResult;
	private long lastResult;
	
	private int numPages;
	private int curPage;

	private CaptureSearchResult result;

	/**
	 * Constructor -- chew search result summaries into format easier for JSPs
	 * to digest.
	 *  
	 * @param httpRequest 
	 * @param wbRequest 
	 * @param results
	 * @param uriConverter 
	 */
	public UIQueryResults(HttpServletRequest httpRequest, 
			WaybackRequest wbRequest, SearchResults results,
			ResultURIConverter uriConverter) {
		super(wbRequest,uriConverter);
		this.searchUrl = wbRequest.get(WaybackRequest.REQUEST_URL);
		this.startTimestamp = Timestamp.parseBefore(results.
				getFilter(WaybackRequest.REQUEST_START_DATE));
		this.endTimestamp = Timestamp.parseAfter(results.getFilter(
				WaybackRequest.REQUEST_END_DATE));
		
		this.resultsReturned = results.getReturnedCount();
		this.resultsMatching = results.getMatchingCount();
		this.resultsPerPage = results.getNumRequested();
		this.firstResult = results.getFirstReturned() + 1;
		this.lastResult = ((firstResult - 1) + resultsReturned);
		this.exactRequestedTimestamp = Timestamp.parseAfter(
				wbRequest.get(WaybackRequest.REQUEST_EXACT_DATE));
		// calculate total pages:
		numPages = (int) Math.ceil((double)resultsMatching/(double)resultsPerPage);
		curPage = (int) Math.floor(((double)(firstResult-1))/(double)resultsPerPage) + 1;
		
	}

	/**
	 * @return Timestamp end cutoff requested by user
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
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
	 * @param timestamp
	 * @return Date for the timestamp string
	 */
	public Date timestampToDate(String timestamp) {
		return new Timestamp(timestamp).getDate();
	}
	
	/**
	 * @param result
	 * @return Date representing captureDate of SearchResult result
	 */
	public Date resultToDate(CaptureSearchResult result) {
		return result.getCaptureDate();
	}
	
	/**
	 * @return Returns the firstResult.
	 */
	public long getFirstResult() {
		return firstResult;
	}

	/**
	 * @return Returns the resultsMatching.
	 */
	public long getResultsMatching() {
		return resultsMatching;
	}

	/**
	 * @return Returns the resultsPerPage.
	 */
	public long getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * @return Returns the resultsReturned.
	 */
	public long getResultsReturned() {
		return resultsReturned;
	}

	/**
	 * @return Returns the curPage.
	 */
	public int getCurPage() {
		return curPage;
	}

	/**
	 * @return Returns the numPages.
	 */
	public int getNumPages() {
		return numPages;
	}

	/**
	 * @param pageNum
	 * @return String URL which will drive browser to search results for a
	 *         different page of results for the same query
	 */
	public String urlForPage(int pageNum) {
		WaybackRequest wbRequest = getWbRequest();
		return wbRequest.getContextPrefix() + "query?" +
			wbRequest.getQueryArguments(pageNum);
	}

	/**
	 * @return Returns the lastResult.
	 */
	public long getLastResult() {
		return lastResult;
	}
	
	/**
	 * @return Returns the exactRequestedTimestamp.
	 */
	public Timestamp getExactRequestedTimestamp() {
		return exactRequestedTimestamp;
	}

	public CaptureSearchResult getResult() {
		return result;
	}

	public void setResult(CaptureSearchResult result) {
		this.result = result;
	}
}
