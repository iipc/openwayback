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
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.resourcestore.ARCRecordToSearchResultAdapter;
import org.archive.wayback.resourcestore.ArcResource;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LiveWebCache {
	private static final Logger LOGGER = Logger.getLogger(
			LiveWebCache.class.getName());

	private long maxFailedCacheMS = 600000;
	private ARCCacheDirectory arcCacheDir = null;
	private URLCacher cacher = null;
	private LiveWebLocalResourceIndex index = null;
	private UrlCanonicalizer canonicalizer = null;
	private ARCRecordToSearchResultAdapter adapter = null;
	
	public LiveWebCache() {
		canonicalizer = new AggressiveUrlCanonicalizer();
		adapter = new ARCRecordToSearchResultAdapter();
		adapter.setCanonicalizer(canonicalizer);
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
	
	private boolean isForgedFailRecentEnough(SearchResult result) {
		String captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		Timestamp t = new Timestamp(captureDate);
		long maxAge = System.currentTimeMillis() - maxFailedCacheMS;
		long failAge = t.getDate().getTime();
		if(failAge > maxAge) {
			return true;
		}
		return false;
	}
	
	private boolean isForgedFailedSearchResult(SearchResult result) {
		String arcFile = result.get(WaybackConstants.RESULT_ARC_FILE);
		return arcFile.equals("-");
	}
	
	private SearchResult forgeFailedSearchResult(URL url) {
		SearchResult result = new SearchResult();

		result.put(WaybackConstants.RESULT_ARC_FILE, "-");
		result.put(WaybackConstants.RESULT_OFFSET, "0");

		result.put(WaybackConstants.RESULT_HTTP_CODE, "0");

		result.put(WaybackConstants.RESULT_MD5_DIGEST, "-");
		result.put(WaybackConstants.RESULT_MIME_TYPE, "-");
		result.put(WaybackConstants.RESULT_CAPTURE_DATE, 
				Timestamp.currentTimestamp().getDateStr());

		result.put(WaybackConstants.RESULT_ORIG_HOST, url.getHost());
		result.put(WaybackConstants.RESULT_REDIRECT_URL, "-");
		result.put(WaybackConstants.RESULT_URL, url.toString());

		String indexUrl;
		try {
			indexUrl = canonicalizer.urlStringToKey(url.toString());
		} catch (URIException e) {
			// not gonna happen...
			e.printStackTrace();
			indexUrl = url.toString();
		}
		result.put(WaybackConstants.RESULT_URL_KEY, indexUrl);
		
		return result;
	}
	
	private Resource getLocalCachedResource(URL url, long maxCacheMS, 
			boolean bUseOlder) throws ResourceNotInArchiveException,
			IOException, LiveDocumentNotAvailableException {
		
		Resource resource = null;
		WaybackRequest wbRequest = makeCacheWBRequest(url,maxCacheMS,bUseOlder);
		
		CaptureSearchResults results = null;
		try {
			SearchResults gresults = index.query(wbRequest);
			if(!(gresults instanceof CaptureSearchResults)) {
				throw new IOException("bad result type...");
			}
			results = (CaptureSearchResults) gresults;
		} catch (ResourceNotInArchiveException e) {
//			e.printStackTrace();
			throw e;
		} catch (WaybackException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		SearchResult result = results.getClosest(wbRequest);
		if(result != null) {
			if(isForgedFailedSearchResult(result)) {
				if(isForgedFailRecentEnough(result)) {
					LOGGER.info(url.toString() + " has failed recently");
					throw new LiveDocumentNotAvailableException("failed prev");
				} else {
					LOGGER.info(url.toString() + " failed a while ago");
					throw new ResourceNotInArchiveException("Nope");
				}
			}
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
		ARCLocation location = null;
		try {
			location = cacher.cache(arcCacheDir, url.toString());
		} catch(LiveDocumentNotAvailableException e) {
			// record the failure, so we can fail early next time:
			SearchResult result = forgeFailedSearchResult(url);
			index.addSearchResult(result);
			LOGGER.info("Added FAIL-URL(" + url.toString() + ") to LiveIndex");
			throw e;
		}
		if(location != null) {
			String name = location.getName();
			long offset = location.getOffset();
			LOGGER.info("Cached URL(" + url.toString() + ") in " +
					"ARC(" + name + ") at (" + offset + ")");
			resource = arcCacheDir.getResource(name, offset);
			// add the result to the index:
			if(resource instanceof ArcResource) {
				ArcResource aResource = (ArcResource) resource;
				ARCRecord record = (ARCRecord) aResource.getArcRecord();
				
				SearchResult result = adapter.adapt(record);
				index.addSearchResult(result);
				LOGGER.info("Added URL(" + url.toString() + ") in " +
						"ARC(" + name + ") at (" + offset + ") to LiveIndex");
				
				// we just read thru the doc in order to index it. Reset:
				resource = arcCacheDir.getResource(name, offset);
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
				LOGGER.info("URL:" + url.toString() + " has not been cached"
						+ " recently enough. Attempting from Live Web");

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
						LOGGER.info("Unable to live-get and older" +
								" is not in cache...throwing LDNAE");
						// rethrow the original...
						throw e1;
					}
					LOGGER.info("Got older version of Cached URL(" + 
							url.toString() + ")");
				} else {
					LOGGER.info("Unable to live-get...throwing LDNAE");
					// rethrow the original...
					throw e1;
				}
			}
		}
		return resource;
	}

	/**
	 * @return the maxFailedCacheMS
	 */
	public long getMaxFailedCacheMS() {
		return maxFailedCacheMS;
	}

	/**
	 * @param maxFailedCacheMS the maxFailedCacheMS to set
	 */
	public void setMaxFailedCacheMS(long maxFailedCacheMS) {
		this.maxFailedCacheMS = maxFailedCacheMS;
	}

	/**
	 * @return the arcCacheDir
	 */
	public ARCCacheDirectory getArcCacheDir() {
		return arcCacheDir;
	}

	/**
	 * @param arcCacheDir the arcCacheDir to set
	 */
	public void setArcCacheDir(ARCCacheDirectory arcCacheDir) {
		this.arcCacheDir = arcCacheDir;
	}

	/**
	 * @return the cacher
	 */
	public URLCacher getCacher() {
		return cacher;
	}

	/**
	 * @param cacher the cacher to set
	 */
	public void setCacher(URLCacher cacher) {
		this.cacher = cacher;
	}

	/**
	 * @return the index
	 */
	public LiveWebLocalResourceIndex getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(LiveWebLocalResourceIndex index) {
		this.index = index;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
		adapter.setCanonicalizer(canonicalizer);
	}
}
