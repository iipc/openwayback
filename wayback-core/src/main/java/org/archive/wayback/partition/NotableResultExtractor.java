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
package org.archive.wayback.partition;

import java.util.Date;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * ObjectFilter of CaptureSearchResult objects, which includes all results
 * but keeps track of "notable" results, in particular, those relative to a
 * specific date: first, previous, closest, next, and last
 * @author brad
 *
 */
public class NotableResultExtractor implements ObjectFilter<CaptureSearchResult>{
	private Date want;
	
	private long closestDist = -1;
	private CaptureSearchResult first;
	private CaptureSearchResult last;
	private CaptureSearchResult prev;
	private CaptureSearchResult closest;
	private CaptureSearchResult next;
	
	public NotableResultExtractor(Date want) {
		this.want = want;
	}
	public int filterObject(CaptureSearchResult o) {
		Date oDate = o.getCaptureDate();
		long tmp = oDate.getTime();
		long cmp = oDate.getTime() - want.getTime();
		long abs = Math.abs(cmp);
		
		last = o;
		if(first == null) {
			// special case for the first record:
			first = o;
			closest = o;
			closestDist = abs;

		} else if(next != null) {
			// we already found everything interesting - no op:
		} else {

			// is this after "want"?
			if(cmp > 0) {
				// is it closer than our last closest?
				if(abs < closestDist) {
					// after, but closer:
					// if closest was before want, it's now prev:
					if(closest.getCaptureDate().before(want)) {
						prev = closest;
					}
					closest = o;
					closestDist = abs;
				} else {
					// we've already found the closest, this is next:
					next = o;
				}
			} else {
				// before "want" & now the closest, old closest is now prev
				prev = closest;
				closest = o;
				closestDist = abs;
			}
		}
		return FILTER_INCLUDE;
	}
	public void complete() {
		
	}
	
	/**
	 * @return the prev
	 */
	public CaptureSearchResult getPrev() {
		return prev;
	}
	/**
	 * @param prev the prev to set
	 */
	public void setPrev(CaptureSearchResult prev) {
		this.prev = prev;
	}
	/**
	 * @return the closest
	 */
	public CaptureSearchResult getClosest() {
		return closest;
	}
	/**
	 * @param closest the closest to set
	 */
	public void setClosest(CaptureSearchResult closest) {
		this.closest = closest;
	}
	/**
	 * @return the next
	 */
	public CaptureSearchResult getNext() {
		return next;
	}
	/**
	 * @param next the next to set
	 */
	public void setNext(CaptureSearchResult next) {
		this.next = next;
	}
	/**
	 * @return the first
	 */
	public CaptureSearchResult getFirst() {
		return first;
	}
	/**
	 * @param first the first to set
	 */
	public void setFirst(CaptureSearchResult first) {
		this.first = first;
	}
	/**
	 * @return the last
	 */
	public CaptureSearchResult getLast() {
		return last;
	}
	/**
	 * @param last the last to set
	 */
	public void setLast(CaptureSearchResult last) {
		this.last = last;
	}

}
