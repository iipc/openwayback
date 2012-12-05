package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.ClosestResultTrackingFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class ClosestTrackingCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private ClosestResultTrackingFilter closestTracker = null;
	public ClosestTrackingCaptureFilterGroup(WaybackRequest request,
			UrlCanonicalizer canonicalizer) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		if(request.isCaptureQueryRequest() ||
				request.isReplayRequest()) {
			closestTracker = 
				new ClosestResultTrackingFilter(request.getReplayDate().getTime());
			chain.addFilter(closestTracker);
		}
	}

	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results)
			throws ResourceNotInArchiveException, BadQueryException,
			AccessControlException {
		if(closestTracker != null) {
			if(results instanceof CaptureSearchResults) {
				CaptureSearchResults cResults = (CaptureSearchResults) results;
				cResults.setClosest(closestTracker.getClosest());
			}
		}
	}
}
