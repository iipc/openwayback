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
package org.archive.wayback.resourceindex.distributed;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RangeGroup implements ResourceIndex {

	private HashMap<String,RangeMember> members = null;
	private String name;
	private String start;
	private String end;

	/**
	 * Constructor
	 * @param name
	 * @param start
	 * @param end
	 */
	public RangeGroup(String name, String start, String end) {
		this.name = name;
		this.start = start;
		this.end = end;
		members = new HashMap<String,RangeMember>();
	}
	
	/**
	 * Update the list of members of this group. Members that disappear are
	 * lost, but members that remain across the operation retain their state.
	 * @param urls
	 */
	public synchronized void setMembers(String[] urls) {
		HashMap<String,RangeMember> newMembers = 
			new HashMap<String,RangeMember>();
		
		for(int i=0; i < urls.length; i++) {
			if(members.containsKey(urls[i])) {
				newMembers.put(urls[i],members.get(urls[i]));
			} else {
				RangeMember newMember = new RangeMember();
				newMember.setUrlBase(urls[i]);
				newMembers.put(urls[i],newMember);
			}
		}
		members = newMembers;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest) throws ResourceIndexNotAvailableException, ResourceNotInArchiveException, BadQueryException, AccessControlException {
		while(true) {
			RangeMember best = findBestMember();
			if(best == null) {
				throw new ResourceIndexNotAvailableException("Unable to find active range for request.");
			}
			best.noteConnectionStart();
			SearchResults results;
			try {

				results = best.query(wbRequest);
				best.noteConnectionSuccess();
				return results;

			} catch (ResourceIndexNotAvailableException e) {

				best.noteConnectionFailure();
				
			} catch (ResourceNotInArchiveException e1) {
				
				// need to catch and rethrow so we do accounting on 
				// activeConnections. ResourceNotInArchive is still a 
				// "connection success".

				best.noteConnectionSuccess();
				throw e1;
			}
		}
	}
	protected synchronized RangeMember findBestMember() {
		RangeMember best = null;
		int lowestWeight = RangeMember.UNUSABLE_WEIGHT;
		Iterator<RangeMember> itr = members.values().iterator();
		while(itr.hasNext()) {
			RangeMember cur = itr.next();
			int curWeight = cur.getWeight();
			if(curWeight != RangeMember.UNUSABLE_WEIGHT) {
				
				if( (lowestWeight == RangeMember.UNUSABLE_WEIGHT)
						|| (curWeight < lowestWeight) ) {

					best = cur;
					lowestWeight = curWeight;
				}
			}
		}
		return best;
	}

	/**
	 * @return Returns the end.
	 */
	public String getEnd() {
		return end;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the start.
	 */
	public String getStart() {
		return start;
	}
	/**
	 * @return Comparator suitable for sorting RangeGroup objects
	 */
	public static Comparator<RangeGroup> getComparator() {
		return new Comparator<RangeGroup>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(RangeGroup r1, RangeGroup r2) {
				return r1.getStart().compareTo(r2.getStart());
			}
		};
	}

	public void shutdown() throws IOException {
		for(RangeMember member : members.values()) {
			member.shutdown();
		}
	}
}
