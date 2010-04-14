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
import org.springframework.beans.factory.BeanNameAware;

/**
 * @author brad
 *
 * AccessPoint subclass which allows no Queries, but makes all replay requests
 * through a LiveWebCache
 *
 */
public class LiveWebAccessPoint extends ServletRequestContext implements BeanNameAware {
	private AccessPoint inner = null;
	private LiveWebCache cache = null;
	private RobotExclusionFilterFactory robotFactory = null;
	private long maxCacheMS = 86400000;
	private String beanName = null;
	private int contextPort = 0;
	private String contextName = null;
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
		this.contextName = "";
		int idx = beanName.indexOf(":");
		if(idx > -1) {
			contextPort = Integer.valueOf(beanName.substring(0,idx));
			contextName = beanName.substring(idx + 1);
		} else {
			try {
				this.contextPort = Integer.valueOf(beanName);
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return the prefix of paths received by this server that are handled by
	 * this WaybackContext, including the trailing '/'
	 */
	public String getContextPath(HttpServletRequest httpRequest) {
		String httpContextPath = httpRequest.getContextPath();
		if(contextName.length() == 0) {
			return httpContextPath + "/";
		}
		return httpContextPath + "/" + contextName + "/";
	}

	
	protected String translateRequest(HttpServletRequest httpRequest, 
			boolean includeQuery) {

		String origRequestPath = httpRequest.getRequestURI();
		if(includeQuery) {
			String queryString = httpRequest.getQueryString();
			if (queryString != null) {
				origRequestPath += "?" + queryString;
			}
		}
		String contextPath = getContextPath(httpRequest);
		if (!origRequestPath.startsWith(contextPath)) {
			if(contextPath.startsWith(origRequestPath)) {
				// missing trailing '/', just omit:
				return "";
			}
			return null;
		}
		return origRequestPath.substring(contextPath.length());
	}
	
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException {
		
		String urlString = translateRequest(httpRequest,true);
		boolean handled = true;
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAccessPoint(inner);
		wbRequest.setContextPrefix(inner.getAbsoluteServerPrefix(httpRequest));
		wbRequest.setServerPrefix(inner.getAbsoluteServerPrefix(httpRequest));
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
			// should we check robots, first?
			if(robotFactory != null) {
				int ruling = robotFactory.get().filterObject(result);
				if(ruling == ExclusionFilter.FILTER_EXCLUDE) {
					throw new RobotAccessControlException(urlString + "is blocked by robots.txt");
				}
			}
			// robots says GO:
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
