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
package org.archive.wayback.core;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.archive.wayback.util.Timestamp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureSearchResults extends SearchResults implements Iterable<CaptureSearchResult> {
	/**
	 * List of UrlSearchResult objects for index records matching a query
	 */
	private LinkedList<CaptureSearchResult> results = 
		new LinkedList<CaptureSearchResult>();
	
	private CaptureSearchResult closest = null;
	
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
			
			if (!results.isEmpty()) {
				results.getLast().setNextResult(result);
				result.setPrevResult(results.getLast());
			}
			
			results.add(result);
		} else {
			if (!results.isEmpty()) {
				results.getFirst().setPrevResult(result);
				result.setNextResult(results.getFirst());
			}
			
			results.add(0,result);
		}
	}	

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public List<CaptureSearchResult> getResults() {
		return results;
	}
	
	public Iterator<CaptureSearchResult> iterator() {
		return results.iterator();
	}

	public int size() {
		return results.size();
	}
	/**
	 * @param closest the closest to set
	 */
	public void setClosest(CaptureSearchResult closest) {
		this.closest = closest;
	}
	/**
	 * @return the closest
	 */
	public CaptureSearchResult getClosest() {
		return closest;
	}
}
