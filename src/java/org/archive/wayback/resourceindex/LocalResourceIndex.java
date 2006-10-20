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
import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.resourceindex.filters.CounterFilter;
import org.archive.wayback.resourceindex.filters.DateRangeFilter;
import org.archive.wayback.resourceindex.filters.EndDateFilter;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.resourceindex.filters.GuardRailFilter;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.resourceindex.filters.StartDateFilter;
import org.archive.wayback.resourceindex.filters.UrlMatchFilter;
import org.archive.wayback.resourceindex.filters.UrlPrefixMatchFilter;
import org.archive.wayback.resourceindex.filters.WindowEndFilter;
import org.archive.wayback.resourceindex.filters.WindowStartFilter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.UrlCanonicalizer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class LocalResourceIndex implements ResourceIndex {

	/**
	 * configuration name for URL prefix to access exclusion service
	 */
	private final static String EXCLUSION_PREFIX = "resourceindex.exclusionurl";

	/**
	 * configuration name for User Agent to send to exclusion service
	 */
	private final static String EXCLUSION_UA = "resourceindex.exclusionua";

	/**
	 * maximum number of records to return
	 */
	private final static int MAX_RECORDS = 1000;
	
	private int maxRecords = MAX_RECORDS;

	private String exclusionUrlPrefix = null;

	private String exclusionUserAgent = null;

	private SearchResultSource source;
	
	private UrlCanonicalizer canonicalizer = new UrlCanonicalizer(); 

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		source = SearchResultSourceFactory.get(p);

		exclusionUrlPrefix = (String) p.get(EXCLUSION_PREFIX);

		exclusionUserAgent = (String) p.get(EXCLUSION_UA);
		
		String maxRecordsConfig = (String) p.get(
				WaybackConstants.MAX_RESULTS_CONFIG_NAME);
		if(maxRecordsConfig != null) {
			maxRecords = Integer.parseInt(maxRecordsConfig);
		}
	}

	private ExclusionFilter getExclusionFilter() {
		if (exclusionUrlPrefix != null) {
			return new ExclusionFilter(exclusionUrlPrefix, exclusionUserAgent);
		}
		return null;
	}

	private void filterRecords(CloseableIterator itr, ObjectFilter filter,
			SearchResults results, boolean forwards) throws IOException {

		while (itr.hasNext()) {
			SearchResult result = (SearchResult) itr.next();
			int ruling = filter.filterObject(result);
			if (ruling == ObjectFilter.FILTER_ABORT) {
				break;
			} else if (ruling == ObjectFilter.FILTER_INCLUDE) {
				results.addSearchResult(result, forwards);
			}
		}
		source.cleanup(itr);
	}

	private String getRequired(WaybackRequest wbRequest, String field,
			String defaultValue) throws BadQueryException {

		String value = wbRequest.get(field);
		if (value == null) {
			if (defaultValue == null) {
				throw new BadQueryException("No " + field + " specified");
			} else {
				value = defaultValue;
			}
		}
		return value;
	}

	private String getRequired(WaybackRequest wbRequest, String field)
			throws BadQueryException {
		return getRequired(wbRequest, field, null);
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

		SearchResults results = new SearchResults(); // return value
														// placeholder

		String startKey; // actual key where search will begin
		String keyUrl; // "purified" URL request
		int startResult; // calculated based on hits/page * pagenum

		// first grab all the info from the WaybackRequest, and validate it:

		int resultsPerPage = wbRequest.getResultsPerPage();
		int pageNum = wbRequest.getPageNum();
		startResult = (pageNum - 1) * resultsPerPage;

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

		String searchUrl = getRequired(wbRequest, WaybackConstants.REQUEST_URL);
		String searchType = getRequired(wbRequest,
				WaybackConstants.REQUEST_TYPE);
		String startDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_START_DATE, Timestamp
						.earliestTimestamp().getDateStr());
		String endDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_END_DATE, Timestamp.latestTimestamp()
						.getDateStr());
		String exactDate = getRequired(wbRequest,
				WaybackConstants.REQUEST_EXACT_DATE, Timestamp
						.latestTimestamp().getDateStr());

		try {
			keyUrl = canonicalizer.urlStringToKey(searchUrl);
		} catch (URIException e) {
			throw new BadQueryException("invalid "
					+ WaybackConstants.REQUEST_URL + " " + searchUrl);
		}

		// set up the common Filters:

		// makes sure we don't inspect too many records: prevents DOS
		GuardRailFilter guardrail = new GuardRailFilter(maxRecords);

		// checks an exclusion service for every matching record
		ExclusionFilter exclusion = getExclusionFilter();

		// this filter will just count how many results matched:
		CounterFilter counter = new CounterFilter();

		if (searchType.equals(WaybackConstants.REQUEST_REPLAY_QUERY)
				|| searchType.equals(WaybackConstants.REQUEST_CLOSEST_QUERY)) {

			ObjectFilterChain forwardFilters = new ObjectFilterChain();
			ObjectFilterChain reverseFilters = new ObjectFilterChain();

			// use the same guardrail for both:
			forwardFilters.addFilter(guardrail);
			reverseFilters.addFilter(guardrail);

			// match URL key:
			forwardFilters.addFilter(new UrlMatchFilter(keyUrl));
			reverseFilters.addFilter(new UrlMatchFilter(keyUrl));

			// stop matching if we hit a date outside the search range:
			forwardFilters.addFilter(new EndDateFilter(endDate));
			reverseFilters.addFilter(new StartDateFilter(startDate));

			// for replay, do not include records that redirect to
			// themselves.. We'll leave this for both closest and replays,
			// because the only application of closest at the moment is 
			// timeline in which case, we don't want to show captures that
			// redirect to themselves in the timeline if they are not viewable.
			SelfRedirectFilter selfRedirectFilter = new SelfRedirectFilter();
			forwardFilters.addFilter(selfRedirectFilter);
			reverseFilters.addFilter(selfRedirectFilter);
			
			// possibly filter via exclusions:
			if (exclusion != null) {
				forwardFilters.addFilter(exclusion);
				reverseFilters.addFilter(exclusion);
			}

			int resultsPerDirection = (int) Math.floor(resultsPerPage / 2);
			if (resultsPerDirection * 2 == resultsPerPage) {
				forwardFilters.addFilter(new WindowEndFilter(
						resultsPerDirection));
			} else {
				forwardFilters.addFilter(new WindowEndFilter(
						resultsPerDirection + 1));
			}
			reverseFilters.addFilter(new WindowEndFilter(resultsPerDirection));

			// add the same counter:
			forwardFilters.addFilter(counter);
			reverseFilters.addFilter(counter);

			startKey = keyUrl + " " + exactDate;

			// first the reverse search:
			try {
				filterRecords(source.getPrefixIterator(startKey), reverseFilters,
						results, true);
				// then the forwards:
				filterRecords(source.getPrefixReverseIterator(startKey),
						forwardFilters, results, false);
			} catch (IOException e) {
				throw new ResourceIndexNotAvailableException(
						e.getLocalizedMessage());
			}

		} else if (searchType.equals(WaybackConstants.REQUEST_URL_QUERY)) {

			// build up the FilterChain(s):
			ObjectFilterChain filters = new ObjectFilterChain();
			filters.addFilter(guardrail);

			filters.addFilter(new UrlMatchFilter(keyUrl));
			filters.addFilter(new EndDateFilter(endDate));
			// possibly filter via exclusions:
			if (exclusion != null) {
				filters.addFilter(exclusion);
			}
			startKey = keyUrl + " " + startDate;
			filters.addFilter(counter);

			// add the start and end windowing filters:
			filters.addFilter(new WindowStartFilter(startResult));
			filters.addFilter(new WindowEndFilter(resultsPerPage));
			try {
				filterRecords(source.getPrefixIterator(startKey), filters, results,
						true);
			} catch (IOException e) {
				throw new ResourceIndexNotAvailableException(
						e.getLocalizedMessage());
			}
			

		} else if (searchType.equals(WaybackConstants.REQUEST_URL_PREFIX_QUERY)) {

			// build up the FilterChain(s):
			ObjectFilterChain filters = new ObjectFilterChain();
			filters.addFilter(guardrail);

			filters.addFilter(new UrlPrefixMatchFilter(keyUrl));
			filters.addFilter(new DateRangeFilter(startDate, endDate));

			// possibly filter via exclusions:
			if (exclusion != null) {
				filters.addFilter(exclusion);
			}
			startKey = keyUrl;
			filters.addFilter(counter);

			// add the start and end windowing filters:
			filters.addFilter(new WindowStartFilter(startResult));
			filters.addFilter(new WindowEndFilter(resultsPerPage));
			try {
				filterRecords(source.getPrefixIterator(startKey), filters, results,
						true);
			} catch (IOException e) {
				throw new ResourceIndexNotAvailableException(
						e.getLocalizedMessage());
			}

		} else {
			throw new BadQueryException("Unknown query type(" + searchType
					+ "), must be " + WaybackConstants.REQUEST_REPLAY_QUERY
					+ ", " + WaybackConstants.REQUEST_CLOSEST_QUERY + ", "
					+ WaybackConstants.REQUEST_URL_QUERY + ", or "
					+ WaybackConstants.REQUEST_URL_PREFIX_QUERY);
		}

		int matched = counter.getNumMatched();
		if (matched == 0) {
			if (exclusion != null && exclusion.blockedAll()) {
				throw new AccessControlException("All results Excluded");
			}
			throw new ResourceNotInArchiveException("the URL " + keyUrl
					+ " is not in the archive.");
		}

		// now we need to set some filter properties on the results:
		results.putFilter(WaybackConstants.REQUEST_URL, keyUrl);
		results.putFilter(WaybackConstants.REQUEST_TYPE, searchType);
		results.putFilter(WaybackConstants.REQUEST_START_DATE, startDate);
		results.putFilter(WaybackConstants.REQUEST_EXACT_DATE, exactDate);
		results.putFilter(WaybackConstants.REQUEST_END_DATE, endDate);

		// window info
		results.putFilter(WaybackConstants.RESULTS_FIRST_RETURNED, String
				.valueOf(startResult));
		results.putFilter(WaybackConstants.RESULTS_REQUESTED, String
				.valueOf(resultsPerPage));

		// how many are actually in the results:
		results.putFilter(WaybackConstants.RESULTS_NUM_RESULTS, String
				.valueOf(matched));

		// how many matched (includes those outside window)
		results.putFilter(WaybackConstants.RESULTS_NUM_RETURNED, String
				.valueOf(results.getResultCount()));

		return results;
	}

}
