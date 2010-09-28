/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ObjectFilterChain implements AND logic to chain together multiple 
 * ObjectFilters into a composite. ABORT and EXCLUDE short circuit the chain, 
 * all filters must INCLUDE for inclusion.
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */

public class ObjectFilterChain<E> implements ObjectFilter<E> {

	private ArrayList<ObjectFilter<E>> filters = null;

	/**
	 * Constructor
	 */
	public ObjectFilterChain() {
		this.filters = new ArrayList<ObjectFilter<E>>();
	}

	/**
	 * @return the filters
	 */
	public ArrayList<ObjectFilter<E>> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(ArrayList<ObjectFilter<E>> filters) {
		this.filters = filters;
	}

	/**
	 * @param filter to be added to the chain. filters are processed in the 
	 * order they are added to the chain.
	 */
	public void addFilter(ObjectFilter<E> filter) {
		filters.add(filter);
	}

	public void addFilters(Collection<ObjectFilter<E>> list) {
		filters.addAll(list);
	}
	
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterObject(E o) {

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
