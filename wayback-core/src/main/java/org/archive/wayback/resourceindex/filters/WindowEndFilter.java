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

import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFitler that includes the first N records seen.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WindowEndFilter<T> implements ObjectFilter<T> {

	private int windowSize = 0;
	private int numSeen = 0;
	private int numReturned = 0;
	
	/**
	 * @param windowSize int number of records to include
	 */
	public WindowEndFilter(int windowSize) {
		this.windowSize = windowSize;
		this.numSeen = 0;
	}
	public int getNumReturned() {
		return numReturned;
	}
	public int getNumSeen() {
		return numSeen;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(T r) {
		numSeen++;
		if(numSeen <= windowSize) {
			numReturned++;
			return FILTER_INCLUDE;
		}
		return FILTER_EXCLUDE;
	}

}
