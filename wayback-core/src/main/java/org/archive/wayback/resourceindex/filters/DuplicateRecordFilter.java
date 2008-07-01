package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * ObjectFilter which omits exact duplicate URL+date records from a stream
 * of CaptureSearchResult.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DuplicateRecordFilter implements ObjectFilter<CaptureSearchResult> {
	private String lastUrl = null;
	private String lastDate = null;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult o) {
		String thisUrl = o.getUrlKey();
		String thisDate = o.getCaptureTimestamp();
		int result = ObjectFilter.FILTER_INCLUDE;
		if(lastUrl != null) {
			if(lastUrl.equals(thisUrl) && thisDate.equals(lastDate)) {
				result = FILTER_EXCLUDE;
			}
		}
		lastUrl = thisUrl;
		lastDate = thisDate;
		return result;
	}
}
