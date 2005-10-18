package org.archive.wayback.simplequeryui;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.ReplayUI;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WMRequest;

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

	// private String nextPageUrl;
	// private String serverBaseUrl;

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

	public Timestamp getEndTimestamp() {
		return endTimestamp;
	}

	public Timestamp getFirstResultTimestamp() {
		return firstResultTimestamp;
	}

	public Timestamp getLastResultTimestamp() {
		return lastResultTimestamp;
	}

	public int getNumResults() {
		return numResults;
	}

	public String getSearchUrl() {
		return searchUrl;
	}

	public Timestamp getStartTimestamp() {
		return startTimestamp;
	}

	public Iterator resultsIterator() {
		return results.iterator();
	}

	public String resultToReplayUrl(ResourceResult result) {
		return replayUI.makeReplayURI(httpServletRequest, result);
	}
}
