/* WindowFilterGroup
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
