package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * ObjectFilter which omits exact duplicate URL+date records from a stream
 * of SearchResults.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DuplicateRecordFilter implements ObjectFilter<SearchResult> {
	private String lastUrl = null;
	private String lastDate = null;
	
	public int filterObject(SearchResult o) {
		String thisUrl = o.getUrl();
		String thisDate = o.getCaptureDate();
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
