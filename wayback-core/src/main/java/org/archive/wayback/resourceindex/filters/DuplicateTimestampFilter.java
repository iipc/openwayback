package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class DuplicateTimestampFilter implements ObjectFilter<CaptureSearchResult> {

	protected String lastTimestamp;
	protected int timestampDedupLength;
	
	public DuplicateTimestampFilter(int timestampDedupLength)
	{
		this.timestampDedupLength = timestampDedupLength;
	}
	
	@Override
	public int filterObject(CaptureSearchResult o) {
		
		if (timestampDedupLength <= 0) {
			return FILTER_INCLUDE;
		}
		
		String timestamp = o.getCaptureTimestamp();
		timestamp = timestamp.substring(0, timestampDedupLength);
		
		boolean sameTimestamp = (lastTimestamp != null) && timestamp.equals(lastTimestamp);
		
		lastTimestamp = timestamp;
		return sameTimestamp ? FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
