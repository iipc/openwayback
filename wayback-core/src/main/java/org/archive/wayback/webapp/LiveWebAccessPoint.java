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
package org.archive.wayback.webapp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.io.arc.ARCRecord;
import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilterFactory;
import org.archive.wayback.accesscontrol.staticmap.StaticMapExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.util.url.UrlOperations;

/**
 * @author brad
 *
 * RequestHandler which satisfies all incoming requests through a LiveWebCache,
 * using an internal AccessPoint to rewrite replayed documents.
 *
 */
public class LiveWebAccessPoint extends LiveWebRequestHandler {
	private static final Logger LOGGER = Logger.getLogger(
			LiveWebAccessPoint.class.getName());
	
	enum PerfStat
	{
		LiveWeb;
	}

	private AccessPoint inner = null;
	private LiveWebCache cache = null;
	private RobotExclusionFilterFactory robotFactory = null;
	private StaticMapExclusionFilterFactory adminFactory = null;
	
	private String perfStatsHeader = null;
	
	private Pattern skipHost = null;
	private int dnsCheckTimeout = 0;
	
	public final static String LIVEWEB_RUNTIME_ERROR_HEADER = "X-Archive-Wayback-Runtime-Liveweb-Error";
	
	private long maxCacheMS = 86400000;
	
	
		
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException {
		
		String urlString = translateRequestPathQuery(httpRequest);
		urlString = UrlOperations.fixupHTTPUrlWithOneSlash(urlString);
		boolean handled = true;
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAccessPoint(inner);

		wbRequest.setLiveWebRequest(true);
		wbRequest.setRequestUrl(urlString);
		
		ArcResource r = null;
		
		try {
			PerfStats.clearAll();
			
			if (inner.isEnablePerfStatsHeader()) {
				PerfStats.timeStart(AccessPoint.PerfStat.Total);
				httpResponse = new PerfWritingHttpServletResponse(httpResponse, AccessPoint.PerfStat.Total, inner.getPerfStatsHeader());
			}
			
			Thread.currentThread().setName("Thread " + 
					Thread.currentThread().getId() + " " + getBeanName() + 
					" handling: " + urlString);
			
			CaptureSearchResult result = new FastCaptureSearchResult();
			
			r = this.getLiveWebResource(result, urlString);
			
			if (r != null) {				
				CaptureSearchResults results = new CaptureSearchResults();
				results.addSearchResult(result);
			
				wbRequest.setReplayTimestamp(result.getCaptureTimestamp());
					
				inner.getReplay().getRenderer(wbRequest, result, r).renderResource(httpRequest, httpResponse, wbRequest, result, r, 
						inner.getUriConverter(), results);	
			} else {
				throw new LiveDocumentNotAvailableException(urlString);
			}

		} catch(WaybackException e) {
			inner.logError(httpResponse, LIVEWEB_RUNTIME_ERROR_HEADER, e, wbRequest);
			inner.getException().renderException(httpRequest, httpResponse, wbRequest, e, inner.getUriConverter());
		
		} catch(Exception e) {
			inner.logError(httpResponse, LIVEWEB_RUNTIME_ERROR_HEADER, e, wbRequest);
		} finally {
			if (r != null) {
				r.close();
			}
		}
		
		return handled;
	}
	
	protected ArcResource getLiveWebResource(CaptureSearchResult result, String urlString) throws WaybackException, IOException
	{
		URL url = null;
		ArcResource r = null;

		if (!urlString.startsWith(UrlOperations.HTTP_SCHEME) &&
			!urlString.startsWith(UrlOperations.HTTPS_SCHEME)) {
			
			// Remove accidental calendar page requests
			if (urlString.startsWith("*/")) {
				urlString = urlString.substring(2);
			}
			
			// Assume http
			urlString = UrlOperations.HTTP_SCHEME + urlString;
		}
				
		try {
			url = new URL(urlString);
		} catch(MalformedURLException e) {
			throw new BadQueryException("Bad URL(" + urlString + ")");
		}
		
		if ((skipHost != null) && skipHost.matcher(url.getHost()).find()) {
			return null;
		}
		
		if ((dnsCheckTimeout > 0) && !checkUrlDns(url, dnsCheckTimeout)) {
			return null;
		}

		result.setOriginalUrl(urlString);
		
		String canonUrl = urlString;
		
		if (inner.getSelfRedirectCanonicalizer() != null) {
			try {
				canonUrl = inner.getSelfRedirectCanonicalizer().urlStringToKey(urlString);
			} catch (IOException io) {
				throw new BadQueryException("Bad URL(" + urlString + ")");
			}
		}
		
		result.setUrlKey(canonUrl);
		
		// check admin excludes first, if configured:
		if(adminFactory != null) {
			ExclusionFilter f = adminFactory.get();
			if(f == null) {
				LOGGER.severe("Unable to get administrative exclusion filter!");
				throw new AdministrativeAccessControlException(urlString + "is blocked.");
			}
			int ruling = f.filterObject(result);
			if(ruling == ExclusionFilter.FILTER_EXCLUDE) {
				throw new AdministrativeAccessControlException(urlString + "is blocked.");
			}				
		}
		// check robots next, if configured
		if(robotFactory != null) {
			int ruling = robotFactory.get().filterObject(result);
			if(ruling == ExclusionFilter.FILTER_EXCLUDE) {
				throw new RobotAccessControlException(urlString + "is blocked by robots.txt");
			}
		}
		// no robots check, or robots.txt says GO:
		//long start = System.currentTimeMillis();
		
		try {
			PerfStats.timeStart(PerfStat.LiveWeb);
			r = (ArcResource) cache.getCachedResource(url, maxCacheMS , false);
		} finally {
			PerfStats.timeEnd(PerfStat.LiveWeb);
		}

		ARCRecord ar = (ARCRecord) r.getArcRecord();
		int status = ar.getStatusCode();
		if ((status == 200) || ((status >= 300) && (status < 400))) {
			result.setCaptureTimestamp(ar.getMetaData().getDate());
			result.setMimeType(ar.getMetaData().getMimetype());
			return r;
		}
		
		return null;
	}
	
	@Override
	public String getLiveWebRedirect(HttpServletRequest request, WaybackRequest wbRequest, WaybackException we)
	{
		if (isLiveWebFound(request, wbRequest)) {
			return LiveWebRedirector.DEFAULT;
		}
		
		return null;
	}
	
	protected boolean checkUrlDns(URL url, int timeout)
	{		
		InetAddress addr = null;
		
		try {
			addr = InetAddress.getByName(url.getHost());
		} catch (UnknownHostException e) {
			return false;
		}
		
		if (addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
			return false;
		}
		
		if (timeout == 0) {
			return true;
		}
		
		try {
			if (addr.isReachable(timeout)) {
				return true;
			}
		} catch (IOException e) {

		}
		
		return false;
	}
	
	private boolean isLiveWebFound(HttpServletRequest request, WaybackRequest wbRequest)
	{
		ArcResource r = null;
		
		String urlString = wbRequest.getRequestUrl();
		
		try {
			r = getLiveWebResource(new FastCaptureSearchResult(), urlString);
			return (r != null);
			
		} catch (Exception e) {
			return false;
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (IOException e) {
					
				}
			}
		}
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

	public StaticMapExclusionFilterFactory getAdminFactory() {
		return adminFactory;
	}

	public void setAdminFactory(StaticMapExclusionFilterFactory adminFactory) {
		this.adminFactory = adminFactory;
	}

	public String getPerfStatsHeader() {
		return perfStatsHeader;
	}

	public void setPerfStatsHeader(String perfStatsHeader) {
		this.perfStatsHeader = perfStatsHeader;
	}

	public String getSkipHost() {
		return skipHost.pattern();
	}

	public void setSkipHost(String skipHost) {
		this.skipHost = Pattern.compile(skipHost);
	}

	public int getDnsCheckTimeout() {
		return dnsCheckTimeout;
	}

	public void setDnsCheckTimeout(int dnsCheckTimeout) {
		this.dnsCheckTimeout = dnsCheckTimeout;
	}
}
