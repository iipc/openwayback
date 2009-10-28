package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filters.ConditionalGetAnnotationFilter;
import org.archive.wayback.resourceindex.filters.DuplicateRecordFilter;
import org.archive.wayback.resourceindex.filters.GuardRailFilter;
import org.archive.wayback.resourceindex.filters.WARCRevisitAnnotationFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class CoreCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;

	public CoreCaptureFilterGroup(LocalResourceIndex index) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		chain.addFilter(new GuardRailFilter(index.getMaxRecords()));
		chain.addFilter(new DuplicateRecordFilter());
		if(index.isDedupeRecords()) {
			chain.addFilter(new WARCRevisitAnnotationFilter());
			chain.addFilter(new ConditionalGetAnnotationFilter());
		}
	}
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results) {
		// TODO: ask guardRailFilter if it aborted processing (too many records)
		// and annotate the results with info about how to continue the request?
	}
}
