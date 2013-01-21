package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.archive.wayback.accesscontrol.robotstxt.redis.RedisRobotsLogic.RedisValue;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;

import com.google.common.io.LimitInputStream;

public class SimpleRedisRobotsCache implements LiveWebCache {
	
	private final static Logger LOGGER = Logger
	.getLogger(SimpleRedisRobotsCache.class.getName());
	
	/* REDIS */
	protected RedisRobotsLogic redisCmds;
	
	/* EXTERNAL CACHE */
	protected LiveWebCache liveweb = null;
	
	final static int STATUS_OK = 200;
	final static int STATUS_ERROR = 502;
	
	final static int MAX_ROBOTS_SIZE = 500000;
	
	final static int ONE_DAY = 60 * 60 * 24;

	private int totalTTL = ONE_DAY * 10;
	private int refreshTTL = ONE_DAY;

	private int notAvailTotalTTL = ONE_DAY * 2;
	private int notAvailRefreshTTL = ONE_DAY / 2;
	
	final static String ROBOTS_TOKEN_EMPTY = "0_ROBOTS_EMPTY";
	
	final static String ROBOTS_TOKEN_ERROR = "0_ROBOTS_ERROR-";
	final static String ROBOTS_TOKEN_ERROR_UNKNOWN = "0_ROBOTS_ERROR-0";
	
	final static String UPDATE_QUEUE_KEY = "robots_update_queue";
	final static int MAX_UPDATE_QUEUE_SIZE = 50000;

	
	@Override
	public Resource getCachedResource(URL urlURL, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
		
		String url = urlURL.toExternalForm();
		
		RedisValue value = null;
		
		try {
			value = redisCmds.getValue(url);
		} catch (LiveWebCacheUnavailableException lw) {
			value = null;
		}
		
		// Use the old liveweb cache, if provided
		if (value == null) {
			RobotsResult result = loadExternal(urlURL, maxCacheMS, bUseOlder);
			
			this.updateCache(result.robots, url, null, result.status, true);
											
			if (result == null || result.status != STATUS_OK) {
				throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
			}
			
			return new RobotsTxtResource(result.robots);
			
		} else {
			
			if (isExpired(value, url, 0)) {	
				redisCmds.pushKey(UPDATE_QUEUE_KEY, url, MAX_UPDATE_QUEUE_SIZE);
			}
			
			String currentRobots = value.value;
			
			if (currentRobots.startsWith(ROBOTS_TOKEN_ERROR)) {
				throw new LiveDocumentNotAvailableException("Robots Error: " + currentRobots);	
			} else if (value.equals(ROBOTS_TOKEN_EMPTY)) {
				currentRobots = "";
			}
			
			return new RobotsTxtResource(currentRobots);	
		}
	}

	@Override
	public void shutdown() {
		if (redisCmds != null) {
			redisCmds.close();
			redisCmds = null;
		}
	}
	
	public boolean isExpired(RedisValue value, String url, int customRefreshTime) {
		
		int maxTime, refreshTime;
		
		boolean isFailedError = value.value.startsWith(ROBOTS_TOKEN_ERROR);
		
		if (isFailedError) {
			String code = value.value.substring(ROBOTS_TOKEN_ERROR.length());
			isFailedError = RobotsContext.isFailedError(code);
		}
		
		if (isFailedError) {
			maxTime = notAvailTotalTTL;
			refreshTime = notAvailRefreshTTL;
		} else {
			maxTime = totalTTL;
			refreshTime = refreshTTL;
		}
		
		if (customRefreshTime > 0) {
			refreshTime = customRefreshTime;
		}
		
		if ((maxTime - value.ttl) >= refreshTime) {
			LOGGER.info("Queue for robot refresh: "
					+ (maxTime - value.ttl) + ">=" + refreshTime + " " + url);
			
			return true;
		}
		
		return false;
	}
	
	class RobotsResult
	{
		String robots;
		int status;
		
		RobotsResult(String robots, int status)
		{
			this.robots = robots;
			this.status = status;
		}
	}
	
	protected RobotsResult loadExternal(URL urlURL, long maxCacheMS, boolean bUseOlder)
	{
		//RobotsContext context = new RobotsContext(url, current, true, true);
		
		ArcResource origResource = null;
		int status = 0;
		String contents = null;
		
		try {
			origResource = (ArcResource)liveweb.getCachedResource(urlURL, maxCacheMS, bUseOlder);
			
			if (origResource.getStatusCode() == STATUS_OK) {		
				//String contentType = origResource.getArcRecord().getHeader().getMimetype();
				
				int numToRead = (int)origResource.getArcRecord().getHeader().getLength();
				
				int maxRead = Math.min(numToRead, MAX_ROBOTS_SIZE);
				
				contents = IOUtils.toString(new LimitInputStream(origResource, maxRead), "UTF-8");
				
				//context.doRead(numToRead, contentType, origResource, "UTF-8");
			}
			
		} catch (Exception e) {
			status = STATUS_ERROR;
		} finally {
			if (origResource != null) {
				try {
					origResource.close();
				} catch (IOException e) {
					
				}
			}
		}
		
		return new RobotsResult(contents, status);
	}
	
	protected void updateCache(final String contents, final String url, final String current, int status, boolean cacheFails) {		
		
		String newRedisValue = null;
		int newTTL = 0;
		boolean ttlOnly = false;
		
		if (status == STATUS_OK) {
			//contents = context.getNewRobots();
			newTTL = totalTTL;
			
			if (contents == null || contents.isEmpty()) {
				newRedisValue = ROBOTS_TOKEN_EMPTY;
			} else if (contents.length() > MAX_ROBOTS_SIZE) {
				newRedisValue = contents.substring(0, MAX_ROBOTS_SIZE);
			} else {
				newRedisValue = contents;
			}
			
		} else {			
			if ((status == 0) || ((status >= 500))) {
				newTTL = notAvailTotalTTL;
				
				// Only Cacheing successful lookups
				if (!cacheFails) {
					return;
				}
			} else {
				newTTL = totalTTL;
			}
			
			newRedisValue = ROBOTS_TOKEN_ERROR + status;
		}
		
		String currentValue = current;	
		
		if (currentValue != null) {
			if (currentValue.equals(newRedisValue)) {
				ttlOnly = true;
			}
			
			// Don't override a valid robots with a timeout error
			if (!((status == 301) || (status == 302)) && !isValidRobots(newRedisValue) && isValidRobots(currentValue)) {
				newTTL = totalTTL;
				ttlOnly = true;
				if (LOGGER.isLoggable(Level.INFO)) {
					LOGGER.info("REFRESH ERROR: " + status + " - Keeping same robots for " + url);
				}
			}
		}
				
		final RedisValue value = new RedisValue((ttlOnly ? null : newRedisValue), newTTL);
		
		redisCmds.updateValue(url, value);
	}
	
	protected boolean isValidRobots(String value) {
		return !value.startsWith(ROBOTS_TOKEN_ERROR) && !value.equals(ROBOTS_TOKEN_EMPTY);
	}
	
	public String[] forceUpdate(String url, int minUpdateTime)
	{
		String current = null;
		
		try {
			RedisValue value = redisCmds.getValue(url);
			
			// Just in case, avoid too many updates
			if ((minUpdateTime > 0) && (value != null) && !isExpired(value, url, minUpdateTime)) {
				return new String[]{value.value};
			}
			
			current = (value != null ? value.value : null);
		} catch (LiveWebCacheUnavailableException lw) {
			current = lw.toString();
		}
		
		RobotsResult result = null;
	
		try {
			result = loadExternal(new URL(url), 0, false);
		} catch (MalformedURLException e) {
			return new String[]{current};
		}
		
		if (result.status == STATUS_OK) {
			this.updateCache(result.robots, url, current, result.status, false);
			
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info("Force updated: " + url);
			}
			
			return new String[]{current, result.robots};
		} else {
			return new String[]{current};
		}
	}

//	public RedisRobotsLogic getRedisConnMan() {
//		return redisCmds.;
//	}

	public void setRedisConnMan(RedisConnectionManager redisConn) {
		this.redisCmds = new RedisRobotsLogic(redisConn);
	}

	public LiveWebCache getLiveweb() {
		return liveweb;
	}

	public void setLiveweb(LiveWebCache liveweb) {
		this.liveweb = liveweb;
	}	
}
