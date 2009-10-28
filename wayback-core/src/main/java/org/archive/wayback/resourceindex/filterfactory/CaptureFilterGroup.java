package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.util.ObjectFilter;

public interface CaptureFilterGroup {
	public List<ObjectFilter<CaptureSearchResult>> getFilters();

	public void annotateResults(SearchResults results) 
	throws ResourceNotInArchiveException, BadQueryException,
	AccessControlException;
}
