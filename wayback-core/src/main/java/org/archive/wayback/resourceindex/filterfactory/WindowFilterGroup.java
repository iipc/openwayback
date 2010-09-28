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

import java.util.List;

import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filters.WindowEndFilter;
import org.archive.wayback.resourceindex.filters.WindowStartFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class WindowFilterGroup<T> {
	int startResult; // calculated based on hits/page * pagenum
	int resultsPerPage;
	int pageNum;
	ObjectFilterChain<T> windowFilters;
	WindowStartFilter<T> startFilter;
	WindowEndFilter<T> endFilter;
	private String requestUrl = null;
	public WindowFilterGroup(WaybackRequest request, LocalResourceIndex index) 
		throws BadQueryException {
		requestUrl = request.getRequestUrl();
		windowFilters = new ObjectFilterChain<T>();
		// first grab all the info from the WaybackRequest, and validate it:
		resultsPerPage = request.getResultsPerPage();
		pageNum = request.getPageNum();

		if (resultsPerPage < 1) {
			throw new BadQueryException("resultsPerPage cannot be < 1");
		}
		if (resultsPerPage > index.getMaxRecords()) {
			throw new BadQueryException("resultsPerPage cannot be > "
					+ index.getMaxRecords());
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
	public List<ObjectFilter<T>> getFilters() {
		return windowFilters.getFilters();
	}

	public void annotateResults(SearchResults results) 
	throws BadQueryException, ResourceNotInArchiveException {
		results.setFirstReturned(startResult);
		results.setNumRequested(resultsPerPage);
		int startSeen = startFilter.getNumSeen();
		if(startSeen == 0) {
				ResourceNotInArchiveException e = 
					new ResourceNotInArchiveException("the URL " + requestUrl
						+ " is not in the archive.");
				e.setCloseMatches(results.getCloseMatches());
				throw e;
		}

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
