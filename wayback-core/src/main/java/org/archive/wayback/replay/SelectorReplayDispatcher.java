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
package org.archive.wayback.replay;

import java.util.List;

import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;

/**
 * ReplayDispatcher instance which uses a configurable ClosestResultSelector
 * to find the best result to show from a given set, and a list of 
 * ReplayRendererSelector to determine how best to replay that result to a user.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SelectorReplayDispatcher implements ReplayDispatcher {
	private List<ReplayRendererSelector> selectors = null;
	private ClosestResultSelector closestSelector = null;
	
	@Override
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource) {
		return getRenderer(wbRequest, result, resource, resource);
	}
	
	@Override
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource) {
		if (selectors != null) {
			for (ReplayRendererSelector selector : selectors) {
				if (selector.canHandle(wbRequest, result, httpHeadersResource,
						payloadResource)) {
					return selector.getRenderer();
				}
			}
		}
		return null;
	}
	
	public CaptureSearchResult getClosest(WaybackRequest wbRequest,
			CaptureSearchResults results) throws BetterRequestException {
		return closestSelector.getClosest(wbRequest, results);
	}
	
	/**
	 * @return the List of ReplayRendererSelector objects configured
	 */
	public List<ReplayRendererSelector> getSelectors() {
		return selectors;
	}
	/**
	 * @param selectors the List of ReplayRendererSelector to use
	 */
	public void setSelectors(List<ReplayRendererSelector> selectors) {
		this.selectors = selectors;
	}
	/**
	 * @param closestSelector the closestSelector to set
	 */
	public void setClosestSelector(ClosestResultSelector closestSelector) {
		this.closestSelector = closestSelector;
	}
	/**
	 * @return the closestSelector
	 */
	public ClosestResultSelector getClosestSelector() {
		return closestSelector;
	}
}
