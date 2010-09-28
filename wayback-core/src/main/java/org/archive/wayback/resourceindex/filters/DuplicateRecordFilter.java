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
