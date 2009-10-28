package org.archive.wayback.resourceindex.filterfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.resourceindex.filters.DateRangeFilter;
import org.archive.wayback.resourceindex.filters.HostMatchFilter;
import org.archive.wayback.resourceindex.filters.SchemeMatchFilter;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.resourceindex.filters.UrlMatchFilter;
import org.archive.wayback.resourceindex.filters.UrlPrefixMatchFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;

public class QueryCaptureFilterGroup implements CaptureFilterGroup {
//	private ObjectFilter<CaptureSearchResult> prefixFilter = null;
//	private ObjectFilter<CaptureSearchResult> dateFilter = null;
//	private ObjectFilter<CaptureSearchResult> selfRedirectFilter = null;
//	private ObjectFilter<CaptureSearchResult> exactHost = null;
//	private ObjectFilter<CaptureSearchResult> exactScheme = null;
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private String requestType = null;
	private String keyUrl = null;
	private String startDate;
	private String endDate;
	private String exactDate;
	/**
	 * List of URL Strings that are "close" to the current request, but not
	 * included in the current CaptureSearchResults.
	 */
	private Map<String,String> closeMatches = new HashMap<String,String>();


	public QueryCaptureFilterGroup(WaybackRequest request, 
			UrlCanonicalizer canonicalizer) 
	throws BadQueryException {
		
		requestType = request.get(WaybackRequest.REQUEST_TYPE);

		// URL-Filters:
		chain = new ObjectFilterChain<CaptureSearchResult>();
		try {
			keyUrl = canonicalizer.urlStringToKey(request.getRequestUrl());
		} catch (URIException e) {
			throw new BadQueryException("Bad request URL(" + 
					request.getRequestUrl() +")");
		}
		if(request.isReplayRequest()) {
			exactDate = request.getReplayTimestamp();
			if(exactDate == null) {
				exactDate = Timestamp.latestTimestamp().getDateStr();
			}
			chain.addFilter(new UrlMatchFilter(keyUrl));
			chain.addFilter(new SelfRedirectFilter(canonicalizer));

		} else if(request.isCaptureQueryRequest()) {
			chain.addFilter(new UrlMatchFilter(keyUrl));
		} else if(request.isUrlQueryRequest()) {
			chain.addFilter(new UrlPrefixMatchFilter(keyUrl));
		}

		// Date-Filters:
		startDate = request.getStartTimestamp();
		if(startDate == null) {
				startDate = Timestamp.earliestTimestamp().getDateStr();
		}
		endDate = request.getEndTimestamp();
		if(endDate == null) {
				endDate = Timestamp.latestTimestamp().getDateStr();
		}
		chain.addFilter(new DateRangeFilter(startDate, endDate));
		
		// Other Filters:
		if(request.isExactHost()) {
			chain.addFilter(
					new HostMatchFilter(
							UrlOperations.urlToHost(request.getRequestUrl()),
							this)
					);
		}

		if(request.isExactScheme()) {
			chain.addFilter(new SchemeMatchFilter(
					UrlOperations.urlToScheme(request.getRequestUrl()),this));
		}
	}

	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results) {

		// set the filter properties on the results:
		results.putFilter(WaybackRequest.REQUEST_URL, keyUrl);
		results.putFilter(WaybackRequest.REQUEST_START_DATE, startDate);
		results.putFilter(WaybackRequest.REQUEST_END_DATE, endDate);
		if(exactDate != null) {
			results.putFilter(WaybackRequest.REQUEST_EXACT_DATE, exactDate);
		}
		results.putFilter(WaybackRequest.REQUEST_TYPE, requestType);
		if(!closeMatches.isEmpty()) {
			results.setCloseMatches(new ArrayList<String>(closeMatches.values()));
		}
	}

	public void addCloseMatch(String host, String closeMatch) {
		closeMatches.put(host, closeMatch);
	}
}
