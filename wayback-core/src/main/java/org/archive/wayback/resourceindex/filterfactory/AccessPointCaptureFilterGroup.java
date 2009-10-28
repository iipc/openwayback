package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.FilePrefixFilter;
import org.archive.wayback.resourceindex.filters.FileRegexFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class AccessPointCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private final static String[] sA = null;

	public AccessPointCaptureFilterGroup(WaybackRequest request) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		List<String> prefixes = null;
		if(request.getAccessPoint() != null) {
			prefixes = request.getAccessPoint().getFilePrefixes();
			if(prefixes != null && prefixes.size() > 0) {
				FilePrefixFilter f = new FilePrefixFilter();
				f.setPrefixes(prefixes.toArray(sA));
				chain.addFilter(f);
			}
			List<String> patterns = request.getAccessPoint().getFilePatterns();
			if(patterns != null && patterns.size() > 0) {
				FileRegexFilter f = new FileRegexFilter();
				f.setPatterns(patterns);
				chain.addFilter(f);
			}
		}
	}
	
	public void annotateResults(SearchResults results)
			throws ResourceNotInArchiveException, BadQueryException,
			AccessControlException {

	}

	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}
}
