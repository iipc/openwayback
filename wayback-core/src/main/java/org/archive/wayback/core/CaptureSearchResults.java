/* CaptureSearchResults
 *
 * $Id$
 *
 * Created on 4:05:33 PM Apr 19, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureSearchResults extends SearchResults {
	/**
	 * List of UrlSearchResult objects for index records matching a query
	 */
	private ArrayList<CaptureSearchResult> results = 
		new ArrayList<CaptureSearchResult>();
	/**
	 * 14-digit timestamp of first capture date contained in the SearchResults
	 */
	private String firstResultTimestamp;
	
	/**
	 * 14-digit timestamp of last capture date contained in the SearchResults
	 */
	private String lastResultTimestamp;

	/**
	 * @return Returns the 14-digit String Timestamp of the first Capture in
	 * this set of SearchResult objects
	 */
	public String getFirstResultTimestamp() {
		return firstResultTimestamp;
	}
	/**
	 * @return Returns the firstResult Date.
	 */
	public Date getFirstResultDate() {
		return new Timestamp(firstResultTimestamp).getDate();
	}

	/**
	 * @return Returns the 14-digit String Timestamp of the last Capture in
	 * this set of SearchResult objects
	 */
	public String getLastResultTimestamp() {
		return lastResultTimestamp;
	}
	
	public Date getLastResultDate() {
		return new Timestamp(lastResultTimestamp).getDate();
	}

	/**
	 * @param wbRequest
	 * @return The closest CaptureSearchResult to the request.
	 */
	public CaptureSearchResult getClosest(WaybackRequest wbRequest) {

		CaptureSearchResult closest = null;
		long closestDistance = 0;
		CaptureSearchResult cur = null;
		long wantTime = Timestamp.parseBefore(wbRequest
				.get(WaybackConstants.REQUEST_EXACT_DATE)).getDate().getTime();

		Iterator<CaptureSearchResult> itr = results.iterator();
		while (itr.hasNext()) {
			cur = itr.next();
			long curDistance = Math.abs(wantTime - 
					cur.getCaptureDate().getTime());

			if ((closest == null) || (curDistance < closestDistance)) {
				closest = cur;
				closestDistance = curDistance;
			}
		}
		return closest;
	}
	/**
	 * append a result
	 * @param result
	 */
	public void addSearchResult(CaptureSearchResult result) {
		addSearchResult(result,true);
	}
	/**
	 * add a result to this results, at either the begginning or at the end,
	 * depending on the append argument
	 * @param result
	 *            SearchResult to add to this set
	 * @param append 
	 */
	public void addSearchResult(CaptureSearchResult result, boolean append) {
		String resultDate = result.getCaptureTimestamp();
		if((firstResultTimestamp == null) || 
				(firstResultTimestamp.compareTo(resultDate) > 0)) {
			firstResultTimestamp = resultDate;
		}
		if((lastResultTimestamp == null) || 
				(lastResultTimestamp.compareTo(resultDate) < 0)) {
			lastResultTimestamp = resultDate;
		}

		if(append) {
			results.add(result);
		} else {
			results.add(0,result);
		}
	}	

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public Iterator<CaptureSearchResult> iterator() {
		return results.iterator();
	}

	public int size() {
		return results.size();
	}
}
