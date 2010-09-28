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

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;

/**
 * SearchResultFilter that excludes records outside of start and end range.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class DateRangeFilter implements ObjectFilter<CaptureSearchResult> {
	
	private String first = null;
	private String last = null;
	
	/**
	 * @param first String earliest date to include
	 * @param last String latest date to include
	 */
	public DateRangeFilter(final String first, final String last) {
		this.first = Timestamp.parseBefore(first).getDateStr();
		this.last = Timestamp.parseAfter(last).getDateStr();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String captureDate = r.getCaptureTimestamp();
		return ((first.compareTo(captureDate) > 0) ||
				(last.compareTo(captureDate) < 0)) ? 
						FILTER_EXCLUDE : FILTER_INCLUDE;
	}
}
