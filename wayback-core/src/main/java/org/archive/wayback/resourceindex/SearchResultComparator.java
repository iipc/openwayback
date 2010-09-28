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
package org.archive.wayback.resourceindex;

import java.util.Comparator;

import org.archive.wayback.core.CaptureSearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultComparator implements Comparator<CaptureSearchResult> {

	private boolean backwards;
	/**
	 * Constructor backwards value of true creates a reverse comparator
	 * @param backwards
	 */
	public SearchResultComparator(boolean backwards) {
		this.backwards = backwards;
	}
	/**
	 * Constructor: compare in normal forwards sort order
	 */
	public SearchResultComparator() {
		backwards = false;
	}
	
	private String objectToKey(CaptureSearchResult r) {
		String urlKey = r.getUrlKey();
		String captureDate = r.getCaptureTimestamp();
		return urlKey + " " + captureDate;
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(CaptureSearchResult o1, CaptureSearchResult o2) {
		String k1 = objectToKey(o1);
		String k2 = objectToKey(o2);
		if(backwards) {
			return k2.compareTo(k1);
		}
		return k1.compareTo(k2);
	}
}
