/* LiveWebAccessPoint
 *
 * $Id$:
 *
 * Created on Apr 1, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.webapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.io.arc.ARCRecord;
import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

/**
 * @author brad
 *
 * RequestHandler which satisfies all incoming requests through a LiveWebCache,
 * using an internal AccessPoint to rewrite replayed documents.
 *
 */
public class LiveWebAccessPoint extends AbstractRequestHandler {
	private AccessPoint inner = null;
	private LiveWebCache cache = null;
	private RobotExclusionFilterFactory robotFactory = null;
	private long maxCacheMS = 86400000;
	
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException {
		
		String urlString = translateRequestPathQuery(httpRequest);

		boolean handled = true;
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAccessPoint(inner);

		wbRequest.setLiveWebRequest(true);
		wbRequest.setRequestUrl(urlString);
		URL url = null;
		try {
			try {
				url = new URL(urlString);
			} catch(MalformedURLException e) {
				throw new BadQueryException("Bad URL(" + urlString + ")");
			}

			CaptureSearchResult result = new CaptureSearchResult();
			result.setOriginalUrl(urlString);
			result.setUrlKey(urlString);
			// check robots first, if configured
			if(robotFactory != null) {
				int ruling = robotFactory.get().filterObject(result);
				if(ruling == ExclusionFilter.FILTER_EXCLUDE) {
					throw new RobotAccessControlException(urlString + "is blocked by robots.txt");
				}
			}
			// no robots check, or robots.txt says GO:
			ArcResource r = (ArcResource) cache.getCachedResource(url, maxCacheMS , false);
			ARCRecord ar = (ARCRecord) r.getArcRecord();
			int status = ar.getStatusCode();
			if((status == 200) || ((status >= 300) && (status < 400))) {
				result.setCaptureTimestamp(ar.getMetaData().getDate());
				result.setMimeType(ar.getMetaData().getMimetype());
				CaptureSearchResults results = new CaptureSearchResults();
				results.addSearchResult(result);
			
				wbRequest.setReplayTimestamp(result.getCaptureTimestamp());
					
				inner.getReplay().getRenderer(wbRequest, result, r).renderResource(
						httpRequest, httpResponse, wbRequest, result, r, 
						inner.getUriConverter(), results);
			} else {
				throw new ResourceNotInArchiveException("Not In Archive - Not on Live web");
			}

		} catch(WaybackException e) {
			inner.getException().renderException(httpRequest, httpResponse, wbRequest,
					e, inner.getUriConverter());
		}
		return handled;
	}

	/**
	 * @return the cache
	 */
	public LiveWebCache getCache() {
		return cache;
	}

	/**
	 * @param cache the cache to set
	 */
	public void setCache(LiveWebCache cache) {
		this.cache = cache;
	}

	/**
	 * @return the robotFactory
	 */
	public RobotExclusionFilterFactory getRobotFactory() {
		return robotFactory;
	}

	/**
	 * @param robotFactory the robotFactory to set
	 */
	public void setRobotFactory(RobotExclusionFilterFactory robotFactory) {
		this.robotFactory = robotFactory;
	}

	/**
	 * @return the inner
	 */
	public AccessPoint getInner() {
		return inner;
	}

	/**
	 * @param inner the inner to set
	 */
	public void setInner(AccessPoint inner) {
		this.inner = inner;
	}
}
