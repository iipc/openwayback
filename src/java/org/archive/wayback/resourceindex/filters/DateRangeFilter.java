/* DateRangeFilter
 *
 * $Id$
 *
 * Created on 3:24:21 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.SearchResultFilter;

/**
 * SearchResultFilter that excludes records outside of start and end range.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class DateRangeFilter extends SearchResultFilter {
	
	private String first = null;
	private String last = null;
	
	/**
	 * @param first String earliest date to include
	 * @param last String latest date to include
	 */
	public DateRangeFilter(final String first, final String last) {
		this.first = first;
		this.last = last;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterSearchResult(SearchResult r) {
		String captureDate = r.get(WaybackConstants.RESULT_CAPTURE_DATE);
		return ((first.compareTo(captureDate) > 0) ||
				(last.compareTo(captureDate) < 0)) ? 
						FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
