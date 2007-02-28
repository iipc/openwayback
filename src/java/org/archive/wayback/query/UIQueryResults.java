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
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;
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

	private Timestamp firstResultTimestamp;

	private Timestamp lastResultTimestamp;
	
	private Timestamp exactRequestedTimestamp;

	private int resultsReturned;
	private int resultsMatching;
	private int resultsPerPage;
	private int firstResult;
	private int lastResult;
	
	private int numPages;
	private int curPage;

	private SearchResults results;
	private HttpServletRequest httpRequest;
	private ResultURIConverter uriConverter;

	/**
	 * Constructor -- chew search result summaries into format easier for JSPs
	 * to digest.
	 *  
	 * @param httpRequest 
	 * @param wbRequest 
	 * @param results
	 * @param uriConverter 
	 * @throws ParseException 
	 */
	public UIQueryResults(HttpServletRequest httpRequest, WaybackRequest wbRequest, SearchResults results,
			ResultURIConverter uriConverter) throws ParseException {
		super(wbRequest);
		this.searchUrl = wbRequest.get(WaybackConstants.RESULT_URL);
		this.startTimestamp = Timestamp.parseBefore(results.
				getFilter(WaybackConstants.REQUEST_START_DATE));
		this.endTimestamp = Timestamp.parseBefore(results.getFilter(
				WaybackConstants.REQUEST_END_DATE));
		
		this.firstResultTimestamp = Timestamp.parseBefore(results
				.getFirstResultDate());
		this.lastResultTimestamp = Timestamp.parseBefore(results
				.getLastResultDate());

		this.resultsReturned = Integer.parseInt(results.getFilter(
				WaybackConstants.RESULTS_NUM_RETURNED));
		this.resultsMatching = Integer.parseInt(results.getFilter(
				WaybackConstants.RESULTS_NUM_RESULTS));
		this.resultsPerPage = Integer.parseInt(results.getFilter(
				WaybackConstants.RESULTS_REQUESTED));
		this.firstResult = Integer.parseInt(results.getFilter(
				WaybackConstants.RESULTS_FIRST_RETURNED)) + 1;
		this.lastResult = ((firstResult - 1) + resultsReturned);
		this.exactRequestedTimestamp = Timestamp.parseAfter(
				wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE));
		// calculate total pages:
		numPages = (int) Math.ceil((double)resultsMatching/(double)resultsPerPage);
		curPage = (int) Math.floor(((double)(firstResult-1))/(double)resultsPerPage) + 1;
		
		this.results = results;
		this.uriConverter = uriConverter;
		this.httpRequest = httpRequest;
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

	/**
	 * @param result
	 * @return Date representing captureDate of SearchResult result
	 */
	public Date resultToDate(SearchResult result) {
		Timestamp t = new Timestamp(result.get(
				WaybackConstants.RESULT_CAPTURE_DATE));
		return t.getDate();
	}
	
	/**
	 * @return Returns the firstResult.
	 */
	public int getFirstResult() {
		return firstResult;
	}

	/**
	 * @return Returns the resultsMatching.
	 */
	public int getResultsMatching() {
		return resultsMatching;
	}

	/**
	 * @return Returns the resultsPerPage.
	 */
	public int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * @return Returns the resultsReturned.
	 */
	public int getResultsReturned() {
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
		//http://localhost:8080/wayback/query?q=url:peagreenboat.com%20type:urlprefixquery&count=20&start_page=4
		return httpRequest.getContextPath() + "/query?" +
			wbRequest.getQueryArguments(pageNum);
	}

	/**
	 * @return Returns the lastResult.
	 */
	public int getLastResult() {
		return lastResult;
	}

	/**
	 * @return Returns the results.
	 */
	public SearchResults getResults() {
		return results;
	}
	
	/**
	 * @return Returns the exactRequestedTimestamp.
	 */
	public Timestamp getExactRequestedTimestamp() {
		return exactRequestedTimestamp;
	}
}
