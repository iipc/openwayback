package org.archive.wayback.accesscontrol.robotstxt.redis;

import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class RedisRobotExclusionFilterFactory extends RobotExclusionFilterFactory {
	
	private boolean cacheFails;

	public boolean isCacheFails() {
		return cacheFails;
	}

	public void setCacheFails(boolean cacheFails) {
		this.cacheFails = cacheFails;
	}

	@Override
	public ExclusionFilter get() {
		return new RedisRobotExclusionFilter(super.getWebCache(), super.getUserAgent(), cacheFails);
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}
}
