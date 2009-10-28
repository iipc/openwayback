package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
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
	public WindowFilterGroup(WaybackRequest request, LocalResourceIndex index) 
		throws BadQueryException {

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
