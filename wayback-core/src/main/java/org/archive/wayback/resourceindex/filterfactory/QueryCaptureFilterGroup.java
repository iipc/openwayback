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
package org.archive.wayback.resourceindex.filterfactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.resourceindex.filters.DateRangeFilter;
import org.archive.wayback.resourceindex.filters.EndDateFilter;
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
		} catch (IOException e) {
			throw new BadQueryException("Bad request URL(" + 
					request.getRequestUrl() +")");
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
		if(request.isReplayRequest()) {
			exactDate = request.getReplayTimestamp();
			if(exactDate == null) {
				exactDate = Timestamp.latestTimestamp().getDateStr();
			}
			chain.addFilter(new UrlMatchFilter(keyUrl));
			chain.addFilter(new SelfRedirectFilter(canonicalizer));
			
			long wantMS = request.getReplayDate().getTime();
			if(request.getAccessPoint().isUseAnchorWindow()) {
				// use AnchorTimestamp, if specified:
				String anchorTS = request.getAnchorTimestamp();
				if(anchorTS != null) {
					wantMS = 
						Timestamp.parseBefore(anchorTS).getDate().getTime();
				}
			}

		} else if(request.isCaptureQueryRequest()) {
			chain.addFilter(new UrlMatchFilter(keyUrl));
			chain.addFilter(new SelfRedirectFilter(canonicalizer));
			// OPTIMIZ: EndDateFilter is a hard stop: ABORT
			//          DateRangeFilter is an INCLUDE/EXCLUDE
			//          one class which EXCLUDEs before startDate, and ABORTs
			//              after endDate would save a compare..
			chain.addFilter(new EndDateFilter(endDate));
			chain.addFilter(new DateRangeFilter(startDate, endDate));
		} else if(request.isUrlQueryRequest()) {
			chain.addFilter(new UrlPrefixMatchFilter(keyUrl));
			chain.addFilter(new DateRangeFilter(startDate, endDate));
		}

		
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
