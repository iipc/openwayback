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

import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which aborts processing when too many records have been 
 * inspected.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class GuardRailFilter implements ObjectFilter<CaptureSearchResult> {
	private static final Logger LOGGER = Logger.getLogger(
			GuardRailFilter.class.getName());
	private int maxRecordsToScan = 0;
	private int recordsScanned = 0;
	
	/**
	 * @param maxRecordsToScan int maximum records to process.
	 */
	public GuardRailFilter(int maxRecordsToScan) {
		this.maxRecordsToScan = maxRecordsToScan;
		this.recordsScanned = 0;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		recordsScanned++;
		if(recordsScanned > maxRecordsToScan) {
			LOGGER.warning("Hit max results on " + r.getUrlKey() + " " 
					+ r.getCaptureTimestamp());
			return FILTER_ABORT;
		}
		return FILTER_INCLUDE;
	}
}
