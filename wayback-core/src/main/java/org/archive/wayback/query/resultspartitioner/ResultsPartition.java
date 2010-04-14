/* ResultsPartition
 *
 * $Id$
 *
 * Created on 2:45:10 PM Jan 11, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.query.resultspartitioner;

import java.util.ArrayList;
import java.util.Iterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated use org.archive.wayback.util.parition.*
 */
public class ResultsPartition {
	private String startDateStr = null; // inclusive
	private String endDateStr = null;   // exclusive
	private String title = null;

	private ArrayList<CaptureSearchResult> matches = null;
	
	/**
	 * @return number of SearchResult objects in this partition
	 */
	public int resultsCount() {
		return matches.size();
	}
	
	/**
	 * Construct a ResultsPartition with the provided range and title
	 * @param startDateStr
	 * @param endDateStr
	 * @param title
	 */
	public ResultsPartition(String startDateStr, String endDateStr,
			String title) {
		this.startDateStr = startDateStr;
		this.endDateStr = endDateStr;
		this.title= title;
		matches = new ArrayList<CaptureSearchResult>();
	}
	
	/**
	 * add all SearchResult objects from the SearchResults which fall
	 * within the time range of this partition into this partition.
	 * @param results
	 */
	public void filter(CaptureSearchResults results) {
		Iterator<CaptureSearchResult> itr = results.iterator();
		while(itr.hasNext()) {
			CaptureSearchResult result = itr.next();
			String captureDate = result.getCaptureTimestamp();
			if((captureDate.compareTo(startDateStr) >= 0) 
					&& (captureDate.compareTo(endDateStr) < 0)) {
				matches.add(result);
			}		
		}
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the matches.
	 */
	public ArrayList<CaptureSearchResult> getMatches() {
		return matches;
	}
}
