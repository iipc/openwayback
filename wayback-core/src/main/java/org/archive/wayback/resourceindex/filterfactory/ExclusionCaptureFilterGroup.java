package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.CounterFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class ExclusionCaptureFilterGroup implements CaptureFilterGroup {

	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private CounterFilter preCounter = null;
	private CounterFilter postCounter = null;
	String requestUrl = null;
	
	public ExclusionCaptureFilterGroup(WaybackRequest request) {
		
		// checks an exclusion service for every matching record
		ObjectFilter<CaptureSearchResult> exclusion = 
			request.getExclusionFilter();
		chain = new ObjectFilterChain<CaptureSearchResult>();
		if(exclusion != null) {
			preCounter = new CounterFilter();
			// count how many results got to the ExclusionFilter:
			chain.addFilter(preCounter);
			chain.addFilter(exclusion);
			// count how many results got past the ExclusionFilter:
			requestUrl = request.getRequestUrl();
		}
		postCounter = new CounterFilter();
		chain.addFilter(postCounter);
	}
	
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results)
			throws AccessControlException, ResourceNotInArchiveException {
		if(postCounter.getNumMatched() == 0) {

			// nothing got to the counter after exclusions. If we have 
			// exclusions (detected by preCounter being non-null, and the 
			// preCounter passed any results, then they were all filtered by
			// the exclusions filter.
			if(preCounter != null && preCounter.getNumMatched() > 0) {
				throw new AccessControlException("All results Excluded");
			}
			ResourceNotInArchiveException e = 
				new ResourceNotInArchiveException("the URL " + requestUrl
					+ " is not in the archive.");
			e.setCloseMatches(results.getCloseMatches());
			throw e;
		}
	}
}
