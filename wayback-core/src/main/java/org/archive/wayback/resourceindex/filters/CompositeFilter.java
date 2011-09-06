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

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * Simple composite ObjectFilter - which includes only if all components include
 * 
 * @author brad
 *
 */
public class CompositeFilter implements ObjectFilter<CaptureSearchResult> {
	private List<ObjectFilter<CaptureSearchResult>> filters;
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		
		for(ObjectFilter<CaptureSearchResult> filter : filters) {
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
	/**
	 * @return the filters
	 */
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return filters;
	}
	/**
	 * @param filters the filters to set
	 */
	public void setFilters(List<ObjectFilter<CaptureSearchResult>> filters) {
		this.filters = filters;
	}

}
