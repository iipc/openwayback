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
package org.archive.wayback.liveweb;

import java.io.IOException;
import java.net.URL;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;


/**
 * Interface to retrieve Resource objects from the live web.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public interface LiveWebCache {
	/**
	 * Fetch a Resource from the live web, or from a cache of the live web.
	 * 
	 * @param url to fetch from the live web.
	 * @param maxCacheMS maximum age of resource to return - optionally honored
	 * @param bUseOlder if true, return documents older than maxCacheMS if
	 *                  a more recent copy is not available.
	 * @return Resource for url
	 * 
	 * @throws LiveDocumentNotAvailableException if the resource cannot be
	 *         retrieved from the live web, but all proxying and caching 
	 *         mechanisms functioned properly
	 * @throws LiveWebCacheUnavailableException if there was a problem either
	 * 		   accessing the live web, in proxying to the live web, or in
	 * 		   maintaining the cache for the live web
	 * @throws LiveWebTimeoutException if there is no response from the live
	 * 		   web cache before a timeout occurred.
	 * @throws IOException for the usual reasons
	 */
	public Resource getCachedResource(URL url, long maxCacheMS, 
			boolean bUseOlder) throws LiveDocumentNotAvailableException,
			LiveWebCacheUnavailableException, LiveWebTimeoutException, IOException;
	/**
	 * closes all resources
	 */
	public void shutdown();
}
