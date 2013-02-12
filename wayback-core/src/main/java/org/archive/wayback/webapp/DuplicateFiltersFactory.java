package org.archive.wayback.webapp;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.DuplicateTimestampFilter;
import org.archive.wayback.util.ObjectFilter;

public class DuplicateFiltersFactory implements CustomResultFilterFactory {
	
	protected int timestampDedupLength = 0;

	public int getTimestampDedupLength() {
		return timestampDedupLength;
	}

	public void setTimestampDedupLength(int timestampDedupLength) {
		this.timestampDedupLength = timestampDedupLength;
	}

	@Override
	public ObjectFilter<CaptureSearchResult> get(AccessPoint ap) {
		return new DuplicateTimestampFilter(timestampDedupLength);
	}
}
