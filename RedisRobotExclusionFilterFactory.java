package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class RedisRobotExclusionFilterFactory extends
		RobotExclusionFilterFactory {
	
	private RedisRobotsCache cache;

	public RedisRobotsCache getCache() {
		return cache;
	}

	public void setCache(RedisRobotsCache cache) {
		this.cache = cache;
	}

	@Override
	public ExclusionFilter get() {
		return new RedisRobotExclusionFilter(super.getWebCache(), super.getUserAgent(), super.getMaxCacheMS());
	}
}
