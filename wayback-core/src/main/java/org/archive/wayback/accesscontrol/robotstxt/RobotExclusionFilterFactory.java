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
package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

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
	public ExclusionFilter get() {
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
