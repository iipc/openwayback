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
package org.archive.wayback.resourceindex.filters;

import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroup;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter that abstracts multiple SearchResultFilters -- if all
 * filters return INCLUDE, then the result is included, but the first to
 * return ABORT or EXCLUDE short-circuits the rest
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeExclusionFilter extends ExclusionFilter {
	//implements ObjectFilter<CaptureSearchResult> {

	private ArrayList<ExclusionFilter> filters = 
		new ArrayList<ExclusionFilter>();
	
	/**
	 * @param filter to be added to the composite.
	 */
	public void addComponent(ExclusionFilter filter) {
		filters.add(filter);
	}
	public void setFilterGroup(ExclusionCaptureFilterGroup filterGroup) {
		this.filterGroup = filterGroup;
		for(ExclusionFilter filter : filters) {
			filter.setFilterGroup(filterGroup);
		}
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		Iterator<ExclusionFilter> itr = filters.iterator();
		while(itr.hasNext()) {
			ObjectFilter<CaptureSearchResult> filter = itr.next();
			if(filter == null) {
				return FILTER_EXCLUDE;
			}
			int result = filter.filterObject(r);
			if(result != FILTER_INCLUDE) {
				return result;
			}
		}
		return FILTER_INCLUDE;
	}
}
