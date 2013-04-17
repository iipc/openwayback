package org.archive.wayback.resourceindex.filters;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class DuplicateTimestampFilter implements ObjectFilter<CaptureSearchResult> {

	final static int WORST_HTTP_CODE = 9999;
	
	protected String lastTimestamp;
	//protected int lastHttpCode = WORST_HTTP_CODE;
	protected int bestHttpCode = WORST_HTTP_CODE;
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
		int httpCode = NumberUtils.toInt(o.getHttpCode(), WORST_HTTP_CODE);
		
		boolean isDupe = false;
		
		if ((lastTimestamp != null) && timestamp.equals(lastTimestamp)) {
			if (httpCode < bestHttpCode) {
				bestHttpCode = httpCode;
			} else {
				isDupe = true;
			}
		} else {
			bestHttpCode = httpCode;
		}
		
		lastTimestamp = timestamp;

		
		return isDupe ? FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
