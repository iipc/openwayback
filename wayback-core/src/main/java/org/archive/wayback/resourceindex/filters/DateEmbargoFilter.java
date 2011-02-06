package org.archive.wayback.resourceindex.filters;

import java.util.Date;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class DateEmbargoFilter implements ObjectFilter<CaptureSearchResult> {
	protected Date embargoDate = null;
	public DateEmbargoFilter(long minAge) {
		embargoDate = new Date(System.currentTimeMillis() - minAge);
	}
	public int filterObject(CaptureSearchResult o) {
		return o.getCaptureDate().compareTo(embargoDate) < 0 
			? FILTER_INCLUDE : FILTER_EXCLUDE;
	}
}
