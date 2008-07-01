/* CounterFilter
 *
 * $Id$
 *
 * Created on 3:20:55 PM Aug 17, 2006.
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

/**
 * SearchResultFilter which INCLUDEs all checked records, but keeps track of 
 * how many were seen during processing.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CounterFilter implements ObjectFilter<CaptureSearchResult> {

	private int numMatched = 0;
	
	/**
	 * @return int total number of records which matched up to this filter.
	 */
	public int getNumMatched() {
		return numMatched;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		numMatched++;
		return FILTER_INCLUDE;
	}
}
