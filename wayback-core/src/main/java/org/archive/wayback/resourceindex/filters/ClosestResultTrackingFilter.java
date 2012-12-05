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
 * Class which observes CaptureSearchResults, keeping track of the closest 
 * result found to a given date. This class has an optimization which ASSUMES
 * results will be seen in increasing date order, so computation can be skipped
 * as soon as dates stop getting closer to the desired date.
 * 
 * @author brad
 *
 */
public class ClosestResultTrackingFilter implements ObjectFilter<CaptureSearchResult> {

	protected boolean found = false;
	protected long wantMS = 0;
	protected long closestDiffMS = 0;
	protected CaptureSearchResult closest = null;
	
	/**
	 * @return the closest
	 */
	public CaptureSearchResult getClosest() {
		return closest;
	}

	/**
	 * @param wantMS the number of MS since the epoch of the desired date.
	 */
	public ClosestResultTrackingFilter(long wantMS) {
		this.wantMS = wantMS;
	}
	
	public int filterObject(CaptureSearchResult o) {
		
		if(found) {
			// dates are now getting further from desired dates, as an 
			// optimization, skip the math: 
			return FILTER_INCLUDE;
		}
		long captureMS = o.getCaptureDate().getTime();
		long diffMS = Math.abs(captureMS - wantMS);

		if(closest == null) {
			// first result to pass, by definition, for now it's the closest:
			closest = o;
			closestDiffMS = diffMS;
			
		} else {
			
			if(closestDiffMS < diffMS) {
				// dates now increasing, start short-circuiting the rest
				found = true;
			} else {
				// this is closer than anything we've seen:		
				closest = o;
				closestDiffMS = diffMS;
			}
		}
		return FILTER_INCLUDE;
	}
}
