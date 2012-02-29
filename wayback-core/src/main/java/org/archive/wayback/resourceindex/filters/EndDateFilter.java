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
		
		int captureDateLength = Math.min(captureDate.length(), endDate.length());

		return (endDate.substring(0, captureDateLength).compareTo(
				captureDate) < 0) ? 
				FILTER_ABORT : FILTER_INCLUDE; 
	}
}
