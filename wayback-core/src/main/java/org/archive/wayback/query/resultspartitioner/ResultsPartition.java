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
