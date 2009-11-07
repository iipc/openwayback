/* DuplicateRecordFilter
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
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
		String thisUrl = o.getOriginalUrl();
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
