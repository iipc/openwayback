package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class RedisRobotExclusionFilterFactory implements ExclusionFilterFactory {
	
	private RedisRobotsCache cache;
	private String userAgent;
	private boolean cacheFails;

	public RedisRobotsCache getCache() {
		return cache;
	}

	public void setCache(RedisRobotsCache cache) {
		this.cache = cache;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public boolean isCacheFails() {
		return cacheFails;
	}

	public void setCacheFails(boolean cacheFails) {
		this.cacheFails = cacheFails;
	}

	@Override
	public ExclusionFilter get() {
		return new RedisRobotExclusionFilter(cache, userAgent, cacheFails);
	}

	@Override
	public void shutdown() {
		this.cache.shutdown();	
	}
}
