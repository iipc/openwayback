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
package org.archive.wayback.resourceindex.distributed;

import java.io.IOException;

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.RemoteResourceIndex;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RangeMember implements ResourceIndex {

	private static long MS_RETRY_INTERVAL = 10 * 1000;
	protected static int UNUSABLE_WEIGHT = -1;
	
	private RemoteResourceIndex index = new RemoteResourceIndex();
	private long lastGoodResponse = System.currentTimeMillis();
	private long lastFailedResponse = 0;
	private int activeConnections = 0;

	protected synchronized int getWeight() {
		int weight = activeConnections;
		if(lastFailedResponse >= lastGoodResponse) {
			long elapsed = System.currentTimeMillis() - lastFailedResponse;
			if(elapsed < MS_RETRY_INTERVAL) {
				weight = UNUSABLE_WEIGHT;
			}
		}
		return weight;
	}
	protected synchronized void noteConnectionStart() {
		activeConnections++;
	}
	protected synchronized void noteConnectionSuccess() {
		activeConnections--;
		lastGoodResponse = System.currentTimeMillis();
	}
	protected synchronized void noteConnectionFailure() {
		activeConnections--;
		lastFailedResponse = System.currentTimeMillis();
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.RemoteResourceIndex#query(org.archive.wayback.core.WaybackRequest)
	 */
	public SearchResults query(WaybackRequest wbRequest) throws ResourceIndexNotAvailableException, ResourceNotInArchiveException, BadQueryException, AccessControlException {
		return index.query(wbRequest);
	}
	/**
	 * @return Returns the urlBase.
	 */
	public String getUrlBase() {
		return index.getSearchUrlBase();
	}
	/**
	 * @param urlBase the urlBase to set
	 */
	public void setUrlBase(String urlBase) {
		index.setSearchUrlBase(urlBase);
	}
	public void shutdown() throws IOException {
		index.shutdown();
	}
}
