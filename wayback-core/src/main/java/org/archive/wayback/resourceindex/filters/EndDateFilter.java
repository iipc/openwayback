/* EndDateFilter
 *
 * $Id$
 *
 * Created on 3:27:33 PM Aug 17, 2006.
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

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;


/**
 * SearchResultFilter which includes all records until 1 is found beyond end 
 * date then it aborts processing. Assumed usage is for URL matches, when 
 * records will be ordered by capture date, and the first record beying the 
 * endDate provided indicates that no further records will possibly match.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class EndDateFilter implements ObjectFilter<CaptureSearchResult> {
	private String endDate = null;
	
	/**
	 * @param endDate String timestamp which marks the end of includable records
	 */
	public EndDateFilter(final String endDate) {
		this.endDate = Timestamp.parseAfter(endDate).getDateStr();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String captureDate = r.getCaptureTimestamp();

		return (endDate.substring(0,captureDate.length()).compareTo(
				captureDate) < 0) ? 
				FILTER_ABORT : FILTER_INCLUDE; 
	}
}
