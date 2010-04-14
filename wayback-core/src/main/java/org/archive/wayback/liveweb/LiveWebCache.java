/* LiveWebCache
 *
 * $Id$
 *
 * Created on 5:26:17 PM Mar 12, 2007.
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
package org.archive.wayback.liveweb;

import java.io.IOException;
import java.net.URL;

import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;


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
	 * @throws IOException for the usual reasons
	 */
	public Resource getCachedResource(URL url, long maxCacheMS, 
			boolean bUseOlder) throws LiveDocumentNotAvailableException,
			LiveWebCacheUnavailableException, IOException;
	/**
	 * closes all resources
	 */
	public void shutdown();
}
