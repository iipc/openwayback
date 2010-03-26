/* RobotExclusionFilter
 *
 * $Id$
 *
 * Created on 3:10:54 PM Mar 14, 2007.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;

/**
 * CaptureSearchResult Filter that uses a LiveWebCache to retrieve robots.txt
 * documents from the live web, and filters SearchResults based on the rules 
 * therein. 
 * 
 * This class caches parsed RobotRules that are retrieved, so using the same 
 * instance to filter multiple SearchResults from the same host will be more
 * efficient.
 * 
 * Instances are expected to be transient for each request: The internally
 * cached StringBuilder is not thread safe.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RobotExclusionFilter extends ExclusionFilter {

	private final static Logger LOGGER = Logger.getLogger(RobotExclusionFilter.class.getName());
	
	private final static String HTTP_PREFIX = "http://";
	private final static String ROBOT_SUFFIX = "/robots.txt";


	private static String WWWN_REGEX = "^www[0-9]+\\.";
	private final static Pattern WWWN_PATTERN = Pattern.compile(WWWN_REGEX);
	private LiveWebCache webCache = null;
	private HashMap<String,RobotRules> rulesCache = null;
	private long maxCacheMS = 0;
	private String userAgent = null;
	private StringBuilder sb = null;
	private final static RobotRules emptyRules = new RobotRules();
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;
	
	/**
	 * Construct a new RobotExclusionFilter that uses webCache to pull 
	 * robots.txt documents. filtering is based on userAgent, and cached 
	 * documents newer than maxCacheMS in the webCache are considered valid.
	 * 
	 * @param webCache LiveWebCache from which documents can be retrieved 
	 * @param userAgent String user agent to use for requests to the live web.
	 * @param maxCacheMS long number of milliseconds to cache documents in the
	 *                   LiveWebCache
	 */
	public RobotExclusionFilter(LiveWebCache webCache, String userAgent,
			long maxCacheMS) {

		rulesCache = new HashMap<String,RobotRules>();

		this.webCache = webCache;
		this.userAgent = userAgent;
		this.maxCacheMS = maxCacheMS;
		sb = new StringBuilder(100);
	}

	private String hostToRobotUrlString(String host) {
		sb.setLength(0);
		sb.append(HTTP_PREFIX).append(host).append(ROBOT_SUFFIX);
		String robotUrl = sb.toString();
		LOGGER.fine("Adding robot URL:" + robotUrl);
		return robotUrl;
	}
	
	/*
	 * Return a List of all robots.txt urls to attempt for this HOST:
	 * If HOST starts with "www.DOMAIN":
	 * 	   [
	 *        http://HOST/robots.txt,
	 *        http://DOMAIN/robots.txt
	 *     ]
	 * If HOST starts with "www[0-9]+.DOMAIN":
	 *     [
	 *        http://HOST/robots.txt,
	 *        http://www.DOMAIN/robots.txt,
	 *        http://DOMAIN/robots.txt
	 *     ]
	 * Otherwise:
	 *     [
	 *        http://HOST/robots.txt,
	 *        http://www.HOST/robots.txt
	 *     ]
	 */
	protected List<String> searchResultToRobotUrlStrings(String resultHost) {
		ArrayList<String> list = new ArrayList<String>();
		list.add(hostToRobotUrlString(resultHost));
		
		if(resultHost.startsWith("www")) {
			if(resultHost.startsWith("www.")) {
				list.add(hostToRobotUrlString(resultHost.substring(4)));
			} else {
				Matcher m = WWWN_PATTERN.matcher(resultHost);
				if(m.find()) {
					String massagedHost = resultHost.substring(m.end());
					list.add(hostToRobotUrlString("www." + massagedHost));
					list.add(hostToRobotUrlString(massagedHost));
				}
			}
		} else {
			list.add(hostToRobotUrlString("www." + resultHost));			
		}
		return list;
	}
	
	private RobotRules getRules(CaptureSearchResult result) {
		RobotRules rules = null;
		RobotRules tmpRules = null;
		String host;
		try {
			host = result.getOriginalHost();
		} catch(Exception e) {
			LOGGER.warning("ROBOT: Failed to get host from("+result.getOriginalUrl()+")");			
			return null;
		}
		List<String> urlStrings = searchResultToRobotUrlStrings(host);
		Iterator<String> itr = urlStrings.iterator();
		String firstUrlString = null;
//		StringBuilder sb = new StringBuilder();
//		for(String ttt : urlStrings) {
//			sb.append("RU(").append(ttt).append(")");
//		}
//		LOGGER.info("RobotUrls for("+host+")"+sb.toString());
		// loop through them all. As soon as we get a response, store that
		// in the cache for the FIRST url we tried and return it..
		// If we get no responses for any of the robot URLs, use "empty" rules,
		// and record that in the cache, too.
		
		while(rules == null && itr.hasNext()) {
			String urlString = (String) itr.next();
			if(firstUrlString == null) {
				firstUrlString = urlString;
			}
			if(rulesCache.containsKey(urlString)) {
				LOGGER.info("ROBOT: Cached("+urlString+")");
				rules = rulesCache.get(urlString);
				if(!urlString.equals(firstUrlString)) {
					LOGGER.info("Adding extra url("+firstUrlString+") for prev cached rules("+urlString+")");
					rulesCache.put(firstUrlString, rules);
				}
			} else {
				try {
					LOGGER.info("ROBOT: NotCached("+urlString+")");
				
					tmpRules = new RobotRules();
					Resource resource = webCache.getCachedResource(new URL(urlString),
							maxCacheMS,true);
					if(resource.getStatusCode() != 200) {
						LOGGER.info("ROBOT: NotAvailable("+urlString+")");
						throw new LiveDocumentNotAvailableException(urlString);
					}
					tmpRules.parse(resource);
					rulesCache.put(firstUrlString,tmpRules);
					rules = tmpRules;
					LOGGER.info("ROBOT: Downloaded("+urlString+")");
					
				} catch (LiveDocumentNotAvailableException e) {
					LOGGER.info("ROBOT: LiveDocumentNotAvailableException("+urlString+")");

				} catch (MalformedURLException e) {
					e.printStackTrace();
					LOGGER.info("ROBOT: MalformedURLException("+urlString+")");
					return null;
				} catch (IOException e) {
					e.printStackTrace(System.err);
					LOGGER.info("ROBOT: IOException("+urlString+"):"+e.getLocalizedMessage());
					return null;
				} catch (LiveWebCacheUnavailableException e) {
					e.printStackTrace();
					LOGGER.info("ROBOT: LiveWebCacheUnavailableException("+urlString+")");
					return null;
				}
			}
		}
		if(rules == null) {
			// special-case, allow empty rules if no longer available.
			rulesCache.put(firstUrlString,emptyRules);
			rules = emptyRules;
			LOGGER.info("No rules available, using emptyRules for:" + firstUrlString);
		}
		return rules;
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		if(!notifiedSeen) {
			filterGroup.setSawRobots();
			notifiedSeen = true;
		}
		int filterResult = ObjectFilter.FILTER_EXCLUDE; 
		RobotRules rules = getRules(r);
		if(rules != null) {
			String resultURL = r.getOriginalUrl();
			URL url;
			try {
				url = new URL(ArchiveUtils.addImpliedHttpIfNecessary(resultURL));
				if(!rules.blocksPathForUA(url.getPath(), userAgent)) {
					if(!notifiedPassed) {
						filterGroup.setPassedRobots();
						notifiedPassed = true;
					}
					filterResult = ObjectFilter.FILTER_INCLUDE;
					LOGGER.fine("ROBOT: ALLOWED("+resultURL+")");
				} else {
					LOGGER.info("ROBOT: BLOCKED("+resultURL+")");
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return filterResult;
	}
}
