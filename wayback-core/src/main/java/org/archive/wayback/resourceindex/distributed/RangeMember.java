/* RangeMember
 *
 * $Id$
 *
 * Created on 3:50:24 PM Jan 25, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
