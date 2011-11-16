package org.archive.wayback.accesscontrol.robotstxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.PerformanceLogger;

public class HRobotExclusionFilter extends ExclusionFilter {

	private final static String ROBOT_SUFFIX = "/robots.txt";
	private final static Logger LOGGER = 
		Logger.getLogger(HRobotExclusionFilter.class.getName());

	// TODO: this is not the right thing!
	private Charset cs = Charset.forName("UTF-8");
	
	private RobotsDirectiveAggregation aggregation = null;
	private LiveWebCache webCache = null;

	private String userAgent = null;
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;
	private static final FixedRobotsDirectives ALLOW_ROBOT_DIRECTIVE = 
		new FixedRobotsDirectives(true);
	
	/**
	 * Construct a new HRobotExclusionFilter that uses webCache to pull 
	 * robots.txt documents. filtering is based on userAgent, and cached 
	 * documents newer than maxCacheMS in the webCache are considered valid.
	 * 
	 * @param webCache LiveWebCache from which documents can be retrieved 
	 * @param userAgent String user agent to use for requests to the live web.
	 * @param maxCacheMS long number of milliseconds to cache documents in the
	 *                   LiveWebCache
	 */
	public HRobotExclusionFilter(LiveWebCache webCache, String userAgent,
			long maxCacheMS) {
		aggregation = new RobotsDirectiveAggregation();
		this.webCache = webCache;
		this.userAgent = userAgent;
	}
	
	private void updateAggregation(String host) 
	throws LiveWebCacheUnavailableException,
	LiveWebTimeoutException, MalformedURLException, IOException {
	
		List<String> missing = aggregation.getMissingRobotUrls(host);
		for(String robotUrl : missing) {
			long start = System.currentTimeMillis();
			Resource resource;
			try {
				resource = webCache.getCachedResource(new URL(robotUrl),
						0,true);
				if(resource.getStatusCode() != 200) {
					LOGGER.info("ROBOT: Non200("+robotUrl+")");
					// consider it an allow:
					aggregation.addDirectives(robotUrl, ALLOW_ROBOT_DIRECTIVE);
				} else {
					InputStreamReader isr = new InputStreamReader(resource, cs);
					BufferedReader br = new BufferedReader(isr);
					Robotstxt robotsTxt = new Robotstxt(br);
					RobotsDirectives directives = robotsTxt.getDirectivesFor(userAgent);
					aggregation.addDirectives(robotUrl, directives);
				}
			} catch (LiveDocumentNotAvailableException e) {
				if(LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("ROBOT: LiveDocumentNotAvailableException("
							+ robotUrl + ")");
				}
				// consider it an allow:
				aggregation.addDirectives(robotUrl, ALLOW_ROBOT_DIRECTIVE);
			}
			long elapsed = System.currentTimeMillis() - start;
			PerformanceLogger.noteElapsed("RobotRequest", elapsed, robotUrl);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		if(!notifiedSeen) {
			if(filterGroup != null) {
				filterGroup.setSawRobots();
			}
			notifiedSeen = true;
		}
		String originalURL = r.getOriginalUrl();
		String path = UrlOperations.getURLPath(originalURL);
		if(path.equals(ROBOT_SUFFIX)) {
			if(!notifiedPassed) {
				if(filterGroup != null) {
					filterGroup.setPassedRobots();
				}
				notifiedPassed = true;
			}
			return ObjectFilter.FILTER_INCLUDE;
		}
		String host = UrlOperations.urlToHost(originalURL);
		boolean updated = false;
		try {
			updateAggregation(host);
			if(!aggregation.isBlocked(path)) {
				if(LOGGER.isLoggable(Level.INFO)) {
					LOGGER.fine("ROBOT: BLOCKED(" + originalURL + ")");
				}
				if(LOGGER.isLoggable(Level.FINE)) {
					LOGGER.finer("ROBOT: ALLOWED(" + originalURL + ")");
				}
				if(!notifiedPassed) {
					if(filterGroup != null) {
						filterGroup.setPassedRobots();
					}
					notifiedPassed = true;
				}
				return ObjectFilter.FILTER_INCLUDE;
			}

//		} catch (LiveDocumentNotAvailableException e) {
		} catch (LiveWebCacheUnavailableException e) {
			LOGGER.severe("ROBOT: LiveWebCacheUnavailableException("
					+ originalURL + ")");
			filterGroup.setLiveWebGone();

		} catch (LiveWebTimeoutException e) {
			LOGGER.severe("ROBOT: LiveDocumentTimedOutException("
					+ originalURL + ")");
			filterGroup.setRobotTimedOut();

		} catch (MalformedURLException e) {

			LOGGER.warning("ROBOT: MalformedURLException(" + 
					originalURL + ")");

		} catch (IOException e) {
			e.printStackTrace();
			return ObjectFilter.FILTER_EXCLUDE;
		}

		if(filterGroup.getRobotTimedOut() || filterGroup.getLiveWebGone()) {
			return ObjectFilter.FILTER_ABORT;
		}
		if(LOGGER.isLoggable(Level.INFO)) {
			LOGGER.fine("ROBOT: BLOCKED(" + originalURL + ")");
		}
		return ObjectFilter.FILTER_EXCLUDE;
	}
}
