package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class RedisRobotExclusionFilterFactory extends
		RobotExclusionFilterFactory {

	@Override
	public ExclusionFilter get() {	
		return new RedisRobotExclusionFilter(super.getWebCache(), super.getUserAgent(), super.getMaxCacheMS());
	}
}
