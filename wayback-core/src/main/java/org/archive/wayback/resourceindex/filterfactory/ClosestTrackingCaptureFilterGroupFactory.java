package org.archive.wayback.resourceindex.filterfactory;

import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.resourceindex.LocalResourceIndex;

public class ClosestTrackingCaptureFilterGroupFactory implements FilterGroupFactory {

	public CaptureFilterGroup getGroup(WaybackRequest request,
			UrlCanonicalizer canonicalizer, LocalResourceIndex index)
			throws BadQueryException {
		return new ClosestTrackingCaptureFilterGroup(request,canonicalizer);
	}

}
