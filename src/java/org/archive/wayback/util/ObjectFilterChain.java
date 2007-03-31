/* ObjectFilterChain
 *
 * $Id$
 *
 * Created on 3:12:35 PM Aug 17, 2006.
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
package org.archive.wayback.util;

import java.util.ArrayList;

/**
 * ObjectFilterChain implements AND logic to chain together multiple 
 * ObjectFilters into a composite. ABORT and EXCLUDE short circuit the chain, 
 * all filters must INCLUDE for inclusion.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ObjectFilterChain implements ObjectFilter {

	private ArrayList<ObjectFilter> filters = null;

	/**
	 * Constructor
	 */
	public ObjectFilterChain() {
		this.filters = new ArrayList<ObjectFilter>();
	}

	/**
	 * @param filter to be added to the chain. filters are processed in the 
	 * order they are added to the chain.
	 */
	public void addFilter(ObjectFilter filter) {
		filters.add(filter);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterObject(Object o) {

		int size = filters.size();
		int result = FILTER_ABORT;
		for (int i = 0; i < size; i++) {
			result = filters.get(i).filterObject(o);
			if (result == FILTER_ABORT) {
				break;
			} else if (result == FILTER_EXCLUDE) {
				break;
			}
		}
		return result;
	}
}
