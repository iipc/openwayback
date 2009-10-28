package org.archive.wayback.resourceindex.filterfactory;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.resourceindex.LocalResourceIndex;

public interface FilterGroupFactory {
	public CaptureFilterGroup getGroup(WaybackRequest request, 
			UrlCanonicalizer canonicalizer, LocalResourceIndex index)
	throws BadQueryException;
}
