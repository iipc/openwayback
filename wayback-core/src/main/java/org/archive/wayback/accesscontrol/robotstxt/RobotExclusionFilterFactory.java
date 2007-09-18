/* RobotExclusionFilterFactory
 *
 * $Id$
 *
 * Created on 3:49:53 PM Mar 14, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.util.ObjectFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RobotExclusionFilterFactory implements ExclusionFilterFactory {

	private LiveWebCache webCache = null;
	private String userAgent = null;
	private long maxCacheMS = 0;

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public ObjectFilter<SearchResult> get() {
		return new RobotExclusionFilter(webCache,userAgent,maxCacheMS);
	}

	/**
	 * @return the webCache
	 */
	public LiveWebCache getWebCache() {
		return webCache;
	}

	/**
	 * @param webCache the webCache to set
	 */
	public void setWebCache(LiveWebCache webCache) {
		this.webCache = webCache;
	}

	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the maxCacheMS
	 */
	public long getMaxCacheMS() {
		return maxCacheMS;
	}

	/**
	 * @param maxCacheMS the maxCacheMS to set
	 */
	public void setMaxCacheMS(long maxCacheMS) {
		this.maxCacheMS = maxCacheMS;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#shutdown()
	 */
	public void shutdown() {
		webCache.shutdown();
	}
}
