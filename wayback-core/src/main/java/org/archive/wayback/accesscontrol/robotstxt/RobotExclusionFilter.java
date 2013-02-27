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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.PerfStats;

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

	private final static Logger LOGGER = 
		Logger.getLogger(RobotExclusionFilter.class.getName());

	protected final static String HTTP_PREFIX = "http://";
	protected final static String ROBOT_SUFFIX = "/robots.txt";

	protected static String WWWN_REGEX = "^www[0-9]+\\.";
	protected final static Pattern WWWN_PATTERN = Pattern.compile(WWWN_REGEX);
	private LiveWebCache webCache = null;
	private HashMap<String,RobotRules> rulesCache = null;
	private long maxCacheMS = 0;
	private String userAgent = null;
	protected StringBuilder sb = null;
	private final static RobotRules emptyRules = new RobotRules();
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;
	
	enum PerfStat
	{
		RobotsFetchTotal,
		RobotsTotal;
	}
	
	protected HashMap<String, Integer> pathsCache = null;
	
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

	protected String hostToRobotUrlString(String host) {
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
				//long start = System.currentTimeMillis();;
				Resource resource = null;
				try {
					PerfStats.timeStart(PerfStat.RobotsFetchTotal);
					
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine("ROBOT: NotCached - Downloading("+urlString+")");
					}
				
					tmpRules = new RobotRules();
					resource = webCache.getCachedResource(new URL(urlString),
							maxCacheMS,true);
					//long elapsed = System.currentTimeMillis() - start;
					//PerformanceLogger.noteElapsed("RobotRequest", elapsed, urlString);

					if(resource.getStatusCode() != 200) {
						LOGGER.info("ROBOT: NotAvailable("+urlString+")");
						throw new LiveDocumentNotAvailableException(urlString);
					}
					tmpRules.parse(resource);					
					rulesCache.put(firstUrlString,tmpRules);
					rules = tmpRules;
					
					if (LOGGER.isLoggable(Level.FINE)) {
						LOGGER.fine("ROBOT: Downloaded("+urlString+")");
					}

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
					if (resource != null) {
						try {
							resource.close();
						} catch (IOException e) {
							
						}
						resource = null;
					}
					//long elapsed = System.currentTimeMillis() - start;
					//PerformanceLogger.noteElapsed("RobotRequest", elapsed, urlString);
					PerfStats.timeEnd(PerfStat.RobotsFetchTotal);
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
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		int filterResult = ObjectFilter.FILTER_EXCLUDE; 
		
		try {
			PerfStats.timeStart(PerfStat.RobotsTotal);
			
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
			
			if (pathsCache == null) {
				pathsCache = new HashMap<String,Integer>();
			} else {
				Integer result = pathsCache.get(r.getUrlKey());
				if (result != null) {
					return result;
				}
			}
			
			RobotRules rules = getRules(r);
			
			if(rules == null) {
				if((filterGroup == null) || (filterGroup.getRobotTimedOut() || filterGroup.getLiveWebGone())) {
					filterResult = ObjectFilter.FILTER_ABORT;
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
			pathsCache.put(r.getUrlKey(), filterResult);
		} finally {
			PerfStats.timeEnd(PerfStat.RobotsTotal, false);
		}
		
		return filterResult;
	}

	public LiveWebCache getWebCache() {
		return webCache;
	}
}
