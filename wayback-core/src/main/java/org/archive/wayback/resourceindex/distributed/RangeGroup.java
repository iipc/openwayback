/* RangeGroup
 *
 * $Id$
 *
 * Created on 3:50:36 PM Jan 25, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
