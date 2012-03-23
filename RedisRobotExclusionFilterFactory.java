package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class RedisRobotExclusionFilterFactory extends RobotExclusionFilterFactory {
	
	private RedisRobotsCache cache;
	private boolean cacheFails;

	public boolean isCacheFails() {
		return cacheFails;
	}

	public void setCacheFails(boolean cacheFails) {
		this.cacheFails = cacheFails;
	}

	@Override
	public ExclusionFilter get() {
		return new RedisRobotExclusionFilter((RedisRobotsCache)super.getWebCache(), super.getUserAgent(), cacheFails);
	}

	@Override
	public void shutdown() {
		this.cache.shutdown();	
	}
}
