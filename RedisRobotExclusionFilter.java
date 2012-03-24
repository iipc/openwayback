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

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.PerformanceLogger;

/**
 *
 * @author ilya
 * @version $Date: 2011-07-07 21:47:51 -0700 (Thu, 07 Jul 2011) $, $Revision: 3485 $
 */
public class RedisRobotExclusionFilter extends ExclusionFilter {

	private final static Logger LOGGER = 
		Logger.getLogger(RedisRobotExclusionFilter.class.getName());
	
	protected final static RobotRules emptyRules = new RobotRules();
	protected static String WWWN_REGEX = "^www[0-9]+\\.";
	protected final static Pattern WWWN_PATTERN = Pattern.compile(WWWN_REGEX);
	
	protected final static String HTTP_PREFIX = "http://";
	protected final static String ROBOT_SUFFIX = "/robots.txt";

	public RedisRobotExclusionFilter(RedisRobotsCache redisCache, String userAgent, boolean cacheFails) {
		
		this.redisCache = redisCache;
		this.sb = new StringBuilder();
		this.userAgent = userAgent;
		this.rulesCache = new HashMap<String, RobotRules>();
		this.cacheFails = cacheFails;
	}
	
	protected StringBuilder sb = null;
	protected boolean cacheFails = false;
	protected RedisRobotsCache redisCache = null;
	protected HashMap<String,RobotRules> rulesCache = null;
	private String userAgent = null;
	
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;

	private boolean useOrigRules = true;

	protected String hostToRobotUrlString(String host) {
		sb.setLength(0);
		sb.append(HTTP_PREFIX);
		sb.append(host);
		if (host.endsWith(".")) {
			sb.deleteCharAt(HTTP_PREFIX.length() + host.length() - 1);
		}
		sb.append(ROBOT_SUFFIX);
		String robotUrl = sb.toString();
		return robotUrl;
	}
	
	protected List<String> searchResultToRobotUrlStrings(String resultHost) {
		ArrayList<String> list = new ArrayList<String>();
		
		if(resultHost.startsWith("www")) {
			if (resultHost.startsWith("www.")) {
				list.add(hostToRobotUrlString(resultHost));
				list.add(hostToRobotUrlString(resultHost.substring(4)));
			} else {
				Matcher m = WWWN_PATTERN.matcher(resultHost);
				if(m.find()) {
					String massagedHost = resultHost.substring(m.end());
					list.add(hostToRobotUrlString("www." + massagedHost));
					list.add(hostToRobotUrlString(massagedHost));
				}
				list.add(hostToRobotUrlString(resultHost));
			}
		} else {	
			list.add(hostToRobotUrlString("www." + resultHost));
			list.add(hostToRobotUrlString(resultHost));
		}
		return list;
	}
	
	protected RobotRules getRules(CaptureSearchResult result) {
		RobotRules rules = null;
		String host;
		try {
			host = result.getOriginalHost();
		} catch(Exception e) {
			LOGGER.warning("ROBOT: Failed to get host from("+result.getOriginalUrl()+")");			
			return null;
		}
		
		ArrayList<String> urlStrings = (ArrayList<String>)searchResultToRobotUrlStrings(host);
		
		String bestUrlString = null;
		
		// First, see if any of the strings are already in the cache
		for (String urlString : urlStrings) {
			if (bestUrlString == null) {
				bestUrlString = urlString;
			}
			
			if (rulesCache.containsKey(urlString)) {
				LOGGER.fine("ROBOT: Cached("+urlString+")");
				rules = rulesCache.get(urlString);
				if(!urlString.equals(bestUrlString)) {
					LOGGER.fine("Adding extra url("+bestUrlString+") for prev cached rules("+urlString+")");
					rulesCache.put(bestUrlString, rules);
				}
				return rules;
			}
		}
		
		long startTime = System.currentTimeMillis();
		
		Resource robots = null;
		
		try {
			robots = redisCache.getCachedRobots(urlStrings, cacheFails, false);
		} catch (LiveWebCacheUnavailableException e1) {
			if (filterGroup != null) {
				filterGroup.setLiveWebGone();
			}
			LOGGER.severe("Live Web Cache Unavail: " + urlStrings.toString());
			return null;
		}
		
		if (robots != null) { 
			rules = new RobotRules();
			try {
				rules.parse(robots);
			} catch (IOException e) {
				rules = emptyRules;
			}
		} else {
			rules = emptyRules;
		}
		
		for (String urlString : urlStrings) {
			rulesCache.put(urlString, rules);
		}
		
		PerformanceLogger.noteElapsed("RobotRequest", System.currentTimeMillis() - startTime);
		
		return rules;
	}
	
	private RobotRules getRulesOrig(CaptureSearchResult result) {
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
				LOGGER.fine("ROBOT: Cached("+urlString+")");
				rules = rulesCache.get(urlString);
				if(!urlString.equals(firstUrlString)) {
					LOGGER.fine("Adding extra url("+firstUrlString+") for prev cached rules("+urlString+")");
					rulesCache.put(firstUrlString, rules);
				}
			} else {
				long start = System.currentTimeMillis();;
				try {
					LOGGER.fine("ROBOT: NotCached - Downloading("+urlString+")");
				
					tmpRules = new RobotRules();
					Resource resource = redisCache.getCachedResource(new URL(urlString),
							cacheFails ? 1 : 0, true);
					//long elapsed = System.currentTimeMillis() - start;
					//PerformanceLogger.noteElapsed("RobotRequest", elapsed, urlString);

					if(resource.getStatusCode() != 200) {
						LOGGER.info("ROBOT: NotAvailable("+urlString+")");
						throw new LiveDocumentNotAvailableException(urlString);
					}
					tmpRules.parse(resource);
					rulesCache.put(firstUrlString,tmpRules);
					rules = tmpRules;
					LOGGER.fine("ROBOT: Downloaded("+urlString+")");

				} catch (LiveDocumentNotAvailableException e) {
					LOGGER.info("ROBOT: LiveDocumentNotAvailableException("+urlString+")");

				} catch (MalformedURLException e) {
//					e.printStackTrace();
					LOGGER.warning("ROBOT: MalformedURLException("+urlString+")");
					return null;
				} catch (IOException e) {
					LOGGER.warning("ROBOT: IOException("+urlString+"):"+e.getLocalizedMessage());
					return null;
				} catch (LiveWebCacheUnavailableException e) {
					LOGGER.severe("ROBOT: LiveWebCacheUnavailableException("+urlString+")");
					if (filterGroup != null) {
						filterGroup.setLiveWebGone();
					}
					return null;
				} catch (LiveWebTimeoutException e) {
					LOGGER.severe("ROBOT: LiveDocumentTimedOutException("+urlString+")");
					if (filterGroup != null) {
						filterGroup.setRobotTimedOut();
					}
					return null;
				} finally {
					long elapsed = System.currentTimeMillis() - start;
					PerformanceLogger.noteElapsed("RobotRequest", elapsed, urlString);
				}
			}
		}
		if(rules == null) {
			// special-case, allow empty rules if no longer available.
			rulesCache.put(firstUrlString,emptyRules);
			rules = emptyRules;
			LOGGER.fine("No rules available, using emptyRules for:" + firstUrlString);
		}
		return rules;
	}

	@Override
	public int filterObject(CaptureSearchResult r) {
		if(!notifiedSeen) {
			if(filterGroup != null) {
				filterGroup.setSawRobots();
			}
			notifiedSeen = true;
		}
		String resultURL = r.getOriginalUrl();
		String path = UrlOperations.getURLPath(resultURL);
		if(path.equals(ROBOT_SUFFIX)) {
			if(!notifiedPassed) {
				if(filterGroup != null) {
					filterGroup.setPassedRobots();
				}
				notifiedPassed = true;
			}
			return ObjectFilter.FILTER_INCLUDE;
		}
		int filterResult = ObjectFilter.FILTER_EXCLUDE; 
		RobotRules rules = (useOrigRules ? getRulesOrig(r) : getRules(r));
		if(rules == null) {
			if((filterGroup == null) || (filterGroup.getRobotTimedOut() || filterGroup.getLiveWebGone())) {
				return ObjectFilter.FILTER_ABORT;
			}
		} else {
			if(!rules.blocksPathForUA(path, userAgent)) {
				if(!notifiedPassed) {
					if(filterGroup != null) {
						filterGroup.setPassedRobots();
					}
					notifiedPassed = true;
				}
				filterResult = ObjectFilter.FILTER_INCLUDE;
				LOGGER.finer("ROBOT: ALLOWED("+resultURL+")");
			} else {
				LOGGER.fine("ROBOT: BLOCKED("+resultURL+")");
			}
		}
		return filterResult;
	}
}