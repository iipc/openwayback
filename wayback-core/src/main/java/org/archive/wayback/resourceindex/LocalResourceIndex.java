/* LocalResourceIndex
 *
 * $Id$
 *
 * Created on 5:02:21 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.adapters.ConditionalGetAnnotationSearchResultAdapter;
import org.archive.wayback.resourceindex.adapters.CaptureToUrlSearchResultAdapter;
import org.archive.wayback.resourceindex.adapters.DeduplicationSearchResultAnnotationAdapter;
import org.archive.wayback.resourceindex.filters.CounterFilter;
import org.archive.wayback.resourceindex.filters.DateRangeFilter;
import org.archive.wayback.resourceindex.filters.DuplicateRecordFilter;
import org.archive.wayback.resourceindex.filters.GuardRailFilter;
import org.archive.wayback.resourceindex.filters.HostMatchFilter;
import org.archive.wayback.resourceindex.filters.SchemeMatchFilter;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.resourceindex.filters.UrlMatchFilter;
import org.archive.wayback.resourceindex.filters.UrlPrefixMatchFilter;
import org.archive.wayback.resourceindex.filters.WindowEndFilter;
import org.archive.wayback.resourceindex.filters.WindowStartFilter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.ObjectFilterIterator;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocalResourceIndex implements ResourceIndex {

	/**
	 * maximum number of records to return
	 */
	private final static int MAX_RECORDS = 1000;
	
	private int maxRecords = MAX_RECORDS;

	protected SearchResultSource source;
	
	private UrlCanonicalizer canonicalizer = null;
	
	private boolean dedupeRecords = false;
	
	private ObjectFilter<CaptureSearchResult> annotater = null;
	
	private ObjectFilter<CaptureSearchResult> filter = null;

	public LocalResourceIndex() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	private CloseableIterator<CaptureSearchResult> getCaptureIterator(String k)
		throws ResourceIndexNotAvailableException {

		CloseableIterator<CaptureSearchResult> captures = 
			source.getPrefixIterator(k);
		if(dedupeRecords) {
			// hack hack!!!
			captures = new AdaptedIterator<CaptureSearchResult, CaptureSearchResult>
				(captures, new ConditionalGetAnnotationSearchResultAdapter());
			captures = new AdaptedIterator<CaptureSearchResult, CaptureSearchResult>
				(captures, new DeduplicationSearchResultAnnotationAdapter());
		}
		return captures;
	}

	private void cleanupIterator(CloseableIterator<? extends SearchResult> itr)
	throws ResourceIndexNotAvailableException {
		try {
			itr.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(
					e.getLocalizedMessage());
		}
	}
	
	public CaptureSearchResults doCaptureQuery(WaybackRequest wbRequest,
			int type) throws ResourceIndexNotAvailableException,
		ResourceNotInArchiveException, BadQueryException,
		AccessControlException {
		
		CaptureSearchResults results = new CaptureSearchResults();

		CaptureQueryFilterState filterState = 
			new CaptureQueryFilterState(wbRequest, canonicalizer, type, 
					getUserFilters(wbRequest));
		String keyUrl = filterState.getKeyUrl();

		CloseableIterator<CaptureSearchResult> itr = getCaptureIterator(keyUrl);
		// set up the common Filters:
		ObjectFilter<CaptureSearchResult> filter = filterState.getFilter();
		itr = new ObjectFilterIterator<CaptureSearchResult>(itr,filter);

		// Windowing:
		WindowFilterState<CaptureSearchResult> window = 
			new WindowFilterState<CaptureSearchResult>(wbRequest);
		ObjectFilter<CaptureSearchResult> windowFilter = window.getFilter();
		itr = new ObjectFilterIterator<CaptureSearchResult>(itr,windowFilter);
		
		
		if(annotater != null) {
			itr = new ObjectFilterIterator<CaptureSearchResult>(itr,annotater);
		}
		
		while(itr.hasNext()) {
			results.addSearchResult(itr.next());
		}
		
		filterState.annotateResults(results);
		window.annotateResults(results);
		cleanupIterator(itr);
		return results;		
	}

	public UrlSearchResults doUrlQuery(WaybackRequest wbRequest)
		throws ResourceIndexNotAvailableException, 
		ResourceNotInArchiveException, BadQueryException, 
		AccessControlException {
		
		UrlSearchResults results = new UrlSearchResults();

		CaptureQueryFilterState filterState = 
			new CaptureQueryFilterState(wbRequest,canonicalizer,
					CaptureQueryFilterState.TYPE_URL, getUserFilters(wbRequest));
		String keyUrl = filterState.getKeyUrl();

		CloseableIterator<CaptureSearchResult> citr = getCaptureIterator(keyUrl);
		// set up the common Filters:
		ObjectFilter<CaptureSearchResult> filter = filterState.getFilter();
		citr = new ObjectFilterIterator<CaptureSearchResult>(citr,filter);
		
		// adapt into UrlSearchResult:
		
		CloseableIterator<UrlSearchResult> itr = 
			CaptureToUrlSearchResultAdapter.adaptCaptureIterator(citr);
		
		// Windowing:
		WindowFilterState<UrlSearchResult> window = 
			new WindowFilterState<UrlSearchResult>(wbRequest);
		ObjectFilter<UrlSearchResult> windowFilter = window.getFilter();
		itr = new ObjectFilterIterator<UrlSearchResult>(itr,windowFilter);
		
		while(itr.hasNext()) {
			results.addSearchResult(itr.next());
		}
		
		filterState.annotateResults(results);
		window.annotateResults(results);
		cleanupIterator(itr);
		
		return results;		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest)
			throws ResourceIndexNotAvailableException,
			ResourceNotInArchiveException, BadQueryException,
			AccessControlException {
		SearchResults results = null; // return value placeholder

		if (wbRequest.isReplayRequest()) {

			results = doCaptureQuery(wbRequest,
					CaptureQueryFilterState.TYPE_REPLAY);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_REPLAY_QUERY);

		} else if (wbRequest.isCaptureQueryRequest()) {

			results = doCaptureQuery(wbRequest, 
					CaptureQueryFilterState.TYPE_CAPTURE);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_CAPTURE_QUERY);

		} else if (wbRequest.isUrlQueryRequest()) {

			results = doUrlQuery(wbRequest);
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_URL_QUERY);

		} else {

			throw new BadQueryException("Unknown query type, must be " 
					+ WaybackRequest.REQUEST_REPLAY_QUERY
					+ ", " + WaybackRequest.REQUEST_CAPTURE_QUERY 
					+ ", or " + WaybackRequest.REQUEST_URL_QUERY);
		}
		return results;
	}

	public void addSearchResults(Iterator<CaptureSearchResult> itr) throws IOException,
		UnsupportedOperationException {
		if(source instanceof UpdatableSearchResultSource) {
			UpdatableSearchResultSource updatable = 
				(UpdatableSearchResultSource) source;
			updatable.addSearchResults(itr,canonicalizer);
		} else {
			throw new UnsupportedOperationException("Underlying " +
					"SearchResultSource is not Updatable.");
		}
	}

	public boolean isUpdatable() {
		return (source instanceof UpdatableSearchResultSource);
	}
	
	/**
	 * @param maxRecords the maxRecords to set
	 */
	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(SearchResultSource source) {
		this.source = source;
	}

	public boolean isDedupeRecords() {
		return dedupeRecords;
	}

	public void setDedupeRecords(boolean dedupeRecords) {
		this.dedupeRecords = dedupeRecords;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	public void shutdown() throws IOException {
		source.shutdown();
	}

	public ObjectFilter<CaptureSearchResult> getAnnotater() {
		return annotater;
	}

	public void setAnnotater(ObjectFilter<CaptureSearchResult> annotater) {
		this.annotater = annotater;
	}

	public ObjectFilter<CaptureSearchResult> getFilter() {
		return filter;
	}

	public void setFilter(ObjectFilter<CaptureSearchResult> filter) {
		this.filter = filter;
	}
	
	public ObjectFilterChain<CaptureSearchResult> getUserFilters(WaybackRequest request) {
		ObjectFilterChain<CaptureSearchResult> userFilters =
			new ObjectFilterChain<CaptureSearchResult>();

		// has the user asked for only results on the exact host specified?
		if(request.isExactHost()) {
			userFilters.addFilter(new HostMatchFilter(
					UrlOperations.urlToHost(request.getRequestUrl())));
		}

		if(request.isExactScheme()) {
			userFilters.addFilter(new SchemeMatchFilter(
					UrlOperations.urlToScheme(request.getRequestUrl())));
		}
		if(filter != null) {
			userFilters.addFilter(filter);
		}

		return userFilters;
	}
	
	private class CaptureQueryFilterState {
		public final static int TYPE_REPLAY = 0;
		public final static int TYPE_CAPTURE = 1;
		public final static int TYPE_URL = 2;
		
		private ObjectFilterChain<CaptureSearchResult> filter = null;
		private CounterFilter finalCounter = null;
		private CounterFilter preExclusionCounter = null;
		private String keyUrl = null;
		private String startDate;
		private String endDate;
		private String exactDate;
		
		public CaptureQueryFilterState(WaybackRequest request, 
				UrlCanonicalizer canonicalizer, int type, 
				ObjectFilterChain<CaptureSearchResult> userFilter)
		throws BadQueryException {
			
			String searchUrl = request.getRequestUrl();
			try {
				keyUrl = canonicalizer.urlStringToKey(searchUrl);
			} catch (URIException e) {
				throw new BadQueryException("invalid "
						+ WaybackRequest.REQUEST_URL + " " + searchUrl);
			}

			filter = new ObjectFilterChain<CaptureSearchResult>();
			startDate = request.getStartTimestamp();
			if(startDate == null) {
					startDate = Timestamp.earliestTimestamp().getDateStr();
			}
			endDate = request.getEndTimestamp();
			if(endDate == null) {
					endDate = Timestamp.latestTimestamp().getDateStr();
			}
			if(type == TYPE_REPLAY) {
				exactDate = request.getReplayTimestamp();
				if(exactDate == null) {
					exactDate = Timestamp.latestTimestamp().getDateStr();
				}
			}
			
			finalCounter = new CounterFilter();
			preExclusionCounter = new CounterFilter();
			DateRangeFilter drFilter = new DateRangeFilter(startDate,endDate);

			// checks an exclusion service for every matching record
			ObjectFilter<CaptureSearchResult> exclusion = 
				request.getExclusionFilter();

			
			// makes sure we don't inspect too many records: prevents DOS
			filter.addFilter(new GuardRailFilter(maxRecords));
			filter.addFilter(new DuplicateRecordFilter());
			
			if(type == TYPE_REPLAY) {
				filter.addFilter(new UrlMatchFilter(keyUrl));
				filter.addFilter(drFilter);
				SelfRedirectFilter selfRedirectFilter= new SelfRedirectFilter();
				selfRedirectFilter.setCanonicalizer(canonicalizer);
				filter.addFilter(selfRedirectFilter);
			} else if(type == TYPE_CAPTURE){
				filter.addFilter(new UrlMatchFilter(keyUrl));
				filter.addFilter(drFilter);
			} else if(type == TYPE_URL) {
				filter.addFilter(new UrlPrefixMatchFilter(keyUrl));				
				filter.addFilter(drFilter);
			} else {
				throw new BadQueryException("Unknown type");
			}

			if(userFilter != null) {
				filter.addFilters(userFilter.getFilters());
			}

			// count how many results got to the ExclusionFilter:
			filter.addFilter(preExclusionCounter);

			if(exclusion != null) {
				filter.addFilter(exclusion);
			}

			// count how many results got past the ExclusionFilter, or how
			// many total matched, if there was no ExclusionFilter:
			filter.addFilter(finalCounter);
		}
		public String getKeyUrl() {
			return keyUrl;
		}
		public ObjectFilter<CaptureSearchResult> getFilter() {
			return filter;
		}
		public void annotateResults(SearchResults results) 
			throws AccessControlException, ResourceNotInArchiveException {

			int matched = finalCounter.getNumMatched();
			if (matched == 0) {
				if (preExclusionCounter != null) {
					if(preExclusionCounter.getNumMatched() > 0) {
						throw new AccessControlException("All results Excluded");
					}
				}
				throw new ResourceNotInArchiveException("the URL " + keyUrl
						+ " is not in the archive.");
			}
			// now we need to set some filter properties on the results:
			results.putFilter(WaybackRequest.REQUEST_URL, keyUrl);
			results.putFilter(WaybackRequest.REQUEST_START_DATE, startDate);
			results.putFilter(WaybackRequest.REQUEST_END_DATE, endDate);
			if(exactDate != null) {
				results.putFilter(WaybackRequest.REQUEST_EXACT_DATE, exactDate);
			}
		}
	}

	private class WindowFilterState<T> {
		int startResult; // calculated based on hits/page * pagenum
		int resultsPerPage;
		int pageNum;
		ObjectFilterChain<T> windowFilters;
		WindowStartFilter<T> startFilter;
		WindowEndFilter<T> endFilter;
		public WindowFilterState(WaybackRequest request) 
			throws BadQueryException {

			windowFilters = new ObjectFilterChain<T>();
			// first grab all the info from the WaybackRequest, and validate it:
			resultsPerPage = request.getResultsPerPage();
			pageNum = request.getPageNum();

			if (resultsPerPage < 1) {
				throw new BadQueryException("resultsPerPage cannot be < 1");
			}
			if (resultsPerPage > maxRecords) {
				throw new BadQueryException("resultsPerPage cannot be > "
						+ maxRecords);
			}
			if (pageNum < 1) {
				throw new BadQueryException("pageNum must be > 0");
			}
			startResult = (pageNum - 1) * resultsPerPage;
			startFilter = new WindowStartFilter<T>(startResult);
			endFilter = new WindowEndFilter<T>(resultsPerPage);
			windowFilters.addFilter(startFilter);
			windowFilters.addFilter(endFilter);
		}
		public ObjectFilter<T> getFilter() {
			return windowFilters;
		}
		public void annotateResults(SearchResults results) 
		throws BadQueryException {
			results.setFirstReturned(startResult);
			results.setNumRequested(resultsPerPage);
			int numSeen = endFilter.getNumSeen();
			if(numSeen == 0) {
				throw new BadQueryException("No results in requested window");
			}
			// how many went by the filters:
			results.setMatchingCount(startFilter.getNumSeen());

			// how many were actually returned:
			results.setReturnedCount(endFilter.getNumReturned());
		}
	}
}
