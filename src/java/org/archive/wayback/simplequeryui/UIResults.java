/* UIResults
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.simplequeryui;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.ReplayUI;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WMRequest;

/**
 * Provides easy access to data required in dispatched QueryUI JSPs.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class UIResults {

	private String searchUrl;

	private Timestamp startTimestamp;

	private Timestamp endTimestamp;

	private Timestamp firstResultTimestamp;

	private Timestamp lastResultTimestamp;

	private int numResults;

	private ResourceResults results;

	private ReplayUI replayUI;

	private HttpServletRequest httpServletRequest;

	/**
	 * @param wmRequest
	 * @param results
	 * @param request
	 * @param replayUI
	 */
	public UIResults(WMRequest wmRequest, ResourceResults results,
			HttpServletRequest request, ReplayUI replayUI) {
		this.searchUrl = wmRequest.getRequestURI().getEscapedURI();
		this.startTimestamp = wmRequest.getStartTimestamp();
		this.endTimestamp = wmRequest.getEndTimestamp();
		this.firstResultTimestamp = results.firstTimestamp();
		this.lastResultTimestamp = results.lastTimestamp();
		this.numResults = results.getNumResults();
		this.results = results;
		this.replayUI = replayUI;
		this.httpServletRequest = request;
	}

	/**
	 * @return Timestamp end cutoff requested by user
	 */
	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @return first Timestamp in returned ResourceResults
	 */
	public Timestamp getFirstResultTimestamp() {
		return firstResultTimestamp;
	}

	/**
	 * @return last Timestamp in returned ResourceResults
	 */
	public Timestamp getLastResultTimestamp() {
		return lastResultTimestamp;
	}

	/**
	 * @return number of ResourceResult objects in response
	 */
	public int getNumResults() {
		return numResults;
	}

	/**
	 * @return URL or URL prefix requested by user
	 */
	public String getSearchUrl() {
		return searchUrl;
	}

	/**
	 * @return Timestamp start cutoff requested by user
	 */
	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @return Iterator of ResourceResults
	 */
	public Iterator resultsIterator() {
		return results.iterator();
	}

	/**
	 * @param result
	 * @return URL string that will replay the specified Resource Result.
	 */
	public String resultToReplayUrl(ResourceResult result) {
		return replayUI.makeReplayURI(httpServletRequest, result);
	}
}
