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
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCWriter;
import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.resourceindex.indexer.ArcIndexer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LiveWebCache implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(
			LiveWebCache.class.getName());

	private ARCCacheDirectory arcCacheDir = null;
	private URLCacher cacher = null;
	private LiveWebLocalResourceIndex index = null;
	
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		index = new LiveWebLocalResourceIndex();
		index.init(p);
		cacher = new URLCacher();
		cacher.init(p);
		arcCacheDir = new ARCCacheDirectory();
		arcCacheDir.init(p);
	}
	
	/**
	 * closes all resources
	 */
	public void shutdown() {
		arcCacheDir.shutdown();
	}
	
	private WaybackRequest makeCacheWBRequest(URL url, long maxCacheMS, 
			boolean bUseOlder) throws URIException {
		WaybackRequest req = new WaybackRequest();
		req.setRequestUrl(url.toString());
		req.put(WaybackConstants.REQUEST_TYPE, 
				WaybackConstants.REQUEST_CLOSEST_QUERY);
		req.put(WaybackConstants.REQUEST_EXACT_DATE,
				Timestamp.currentTimestamp().getDateStr());
		Timestamp earliest = null;
		if(bUseOlder) {
			earliest = Timestamp.earliestTimestamp();
		} else {
			Date d = new Date(System.currentTimeMillis() - maxCacheMS);
			earliest = new Timestamp(d);
		}
		req.put(WaybackConstants.REQUEST_START_DATE,earliest.getDateStr());
		// for now, assume all live web requests are only satisfiable by the 
		// exact host -- no massaging.
		req.put(WaybackConstants.REQUEST_EXACT_HOST_ONLY,
				WaybackConstants.REQUEST_YES);
		return req;
	}
	
	private Resource getLocalCachedResource(URL url, long maxCacheMS, 
			boolean bUseOlder) throws ResourceNotInArchiveException,
			IOException {
		
		Resource resource = null;
		WaybackRequest wbRequest = makeCacheWBRequest(url,maxCacheMS,bUseOlder);
		
		SearchResults results;
		try {
			results = index.query(wbRequest);
		} catch (ResourceNotInArchiveException e) {
			e.printStackTrace();
			throw e;
		} catch (WaybackException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		SearchResult result = results.getClosest(wbRequest);
		if(result != null) {
			String name = (String) result.get(WaybackConstants.RESULT_ARC_FILE);
			long offset = Long.parseLong(
					(String) result.get(WaybackConstants.RESULT_OFFSET));
			resource = arcCacheDir.getResource(name, offset);
		}
		return resource;
	}
	
	private Resource getLiveCachedResource(URL url)
		throws LiveDocumentNotAvailableException, IOException {
		
		Resource resource = null;
		
		LOGGER.info("Caching URL(" + url.toString() + ")");
		ARCWriter writer = arcCacheDir.getWriter();
		ARCLocation location = cacher.cache(writer, url.toString());
		arcCacheDir.returnWriter(writer);
		if(location != null) {
			String name = location.getName();
			long offset = location.getOffset();
			LOGGER.info("Cached URL(" + url.toString() + ") in " +
					"ARC(" + name + ") at (" + offset + ")");
			resource = arcCacheDir.getResource(name, offset);
			// add the result to the index:
			ARCRecord record = (ARCRecord) resource.getArcRecord();
			try {
				
				SearchResult result = ArcIndexer.arcRecordToSearchResult(record);
				index.addSearchResult(result);
				LOGGER.info("Added URL(" + url.toString() + ") in " +
						"ARC(" + name + ") at (" + offset + ") to LiveIndex");
				
			} catch (ParseException e) {
				// TODO: This case could be a big problem -- we might be unable
				// to store the fact that we have a local copy. That means we
				// could be slamming somebody else's site.
				e.printStackTrace();
				throw new IOException(e.getLocalizedMessage());
			}
		}
		
		return resource;
	}
	
	/**
	 * @param url
	 * @param maxCacheMS
	 * @param bUseOlder
	 * @return Resource for url
	 * 
	 * @throws LiveDocumentNotAvailableException
	 * @throws IOException
	 */
	public Resource getCachedResource(URL url, long maxCacheMS, 
			boolean bUseOlder) throws LiveDocumentNotAvailableException, 
			IOException {
		
		Resource resource = null;
		try {
			resource = getLocalCachedResource(url, maxCacheMS, false);
			LOGGER.info("Using Cached URL(" + url.toString() + ")");
			
		} catch(ResourceNotInArchiveException e) {
			try {
				resource = getLiveCachedResource(url);
			} catch (LiveDocumentNotAvailableException e1) {
				if(bUseOlder) {
					// we don't have a copy that satisfies the "ideal" maxAge,
					// but the file isn't on the live web, and the caller has
					// asked to use an older cached copy if a fresh one isn't
					// available.
					LOGGER.info("Second Cached attempt for URL(" + 
							url.toString() + ") allowing older...");
					try {
						resource = getLocalCachedResource(url, maxCacheMS, true);
					} catch (ResourceNotInArchiveException e2) {
						// rethrow the original...
						throw e1;
					}
					LOGGER.info("Got older version of Cached URL(" + 
							url.toString() + ")");
				} else {
					// rethrow the original...
					throw e1;
				}
			}
		}
		return resource;
	}
}
