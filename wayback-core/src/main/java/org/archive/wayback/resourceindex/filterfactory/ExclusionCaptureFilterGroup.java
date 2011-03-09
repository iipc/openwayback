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
package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.exception.RobotNotAvailableException;
import org.archive.wayback.exception.RobotTimedOutAccessControlException;
import org.archive.wayback.resourceindex.filters.CounterFilter;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class ExclusionCaptureFilterGroup implements CaptureFilterGroup {

	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private CounterFilter preCounter = null;
	private CounterFilter postCounter = null;
	String requestUrl = null;
	private boolean sawRobots = false;
	private boolean passedRobots = false;
	private boolean robotTimedOut = false;
	private boolean liveWebGone = false;
	private boolean sawAdministrative = false;
	private boolean passedAdministrative = false;
	
	public ExclusionCaptureFilterGroup(WaybackRequest request) {
		
		// checks an exclusion service for every matching record
		ExclusionFilter exclusion = request.getExclusionFilter();
		chain = new ObjectFilterChain<CaptureSearchResult>();
		if(exclusion != null) {
			exclusion.setFilterGroup(this);
//			preCounter = new CounterFilter();
//			// count how many results got to the ExclusionFilter:
//			chain.addFilter(preCounter);
			chain.addFilter(exclusion);
			// count how many results got past the ExclusionFilter:
			requestUrl = request.getRequestUrl();
		}
//		postCounter = new CounterFilter();
//		chain.addFilter(postCounter);
	}
	
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results)
			throws AccessControlException, ResourceNotInArchiveException,
			RobotNotAvailableException {
		if(robotTimedOut) {
			throw new RobotTimedOutAccessControlException("Unable to check" +
					" robots.txt for " + requestUrl);
		}
		if(liveWebGone) {
			throw new RobotNotAvailableException("The URL " + requestUrl +
			" is blocked by the sites robots.txt file");
		}
		if(sawRobots && !passedRobots) {
			throw new RobotAccessControlException("The URL " + requestUrl +
					" is blocked by the sites robots.txt file");
		}
		if(sawAdministrative && !passedAdministrative) {
			throw new AdministrativeAccessControlException(requestUrl +
					"  is not available in the Wayback Machine.");
		}
	}

	public void setPassedRobots() {
		passedRobots = true;
	}
	public void setSawRobots() {
		sawRobots = true;
	}

	public void setPassedAdministrative() {
		passedAdministrative = true;
	}
	public void setSawAdministrative() {
		sawAdministrative = true;
	}

	public void setRobotTimedOut() {
		robotTimedOut = true;
	}
	public boolean getRobotTimedOut() {
		return robotTimedOut;
	}

	public void setLiveWebGone() {
		liveWebGone = true;
	}
	public boolean getLiveWebGone() {
		return liveWebGone;
	}
}
