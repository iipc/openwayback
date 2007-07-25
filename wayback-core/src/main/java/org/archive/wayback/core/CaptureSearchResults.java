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

import java.text.ParseException;
import java.util.Iterator;

import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureSearchResults extends SearchResults {
	public String getResultsType() {
		return WaybackConstants.RESULTS_TYPE_CAPTURE;
	}
	/**
	 * append a result
	 * @param result
	 */
	public void addSearchResult(final SearchResult result) {
		addSearchResult(result,true);
	}
	/**
	 * add a result to this results, at either the begginning or at the end,
	 * depending on the append argument
	 * @param result
	 *            SearchResult to add to this set
	 * @param append 
	 */
	public void addSearchResult(final SearchResult result, final boolean append) {
		String resultDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		if((firstResultDate == null) || 
				(firstResultDate.compareTo(resultDate) > 0)) {
			firstResultDate = resultDate;
		}
		if((lastResultDate == null) || 
				(lastResultDate.compareTo(resultDate) < 0)) {
			lastResultDate = resultDate;
		}
		addSearchResultRaw(result,append);
	}
	/**
	 * @param wbRequest
	 * @return The closest SearchResult to the request.
	 * @throws ParseException
	 */
	public SearchResult getClosest(WaybackRequest wbRequest) {

		SearchResult closest = null;
		long closestDistance = 0;
		SearchResult cur = null;
		Timestamp wantTimestamp;
		wantTimestamp = Timestamp.parseBefore(wbRequest
				.get(WaybackConstants.REQUEST_EXACT_DATE));

		Iterator<SearchResult> itr = results.iterator();
		while (itr.hasNext()) {
			cur = itr.next();
			long curDistance;
			Timestamp curTimestamp = Timestamp.parseBefore(cur
					.get(WaybackConstants.RESULT_CAPTURE_DATE));
			curDistance = curTimestamp.absDistanceFromTimestamp(wantTimestamp);
			
			if ((closest == null) || (curDistance < closestDistance)) {
				closest = cur;
				closestDistance = curDistance;
			}
		}
		return closest;
	}	
}
