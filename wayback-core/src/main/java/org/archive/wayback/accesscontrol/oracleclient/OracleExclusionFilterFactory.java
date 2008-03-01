package org.archive.wayback.accesscontrol.oracleclient;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.accesscontrol.robotstxt.RobotExclusionFilterFactory;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

public class OracleExclusionFilterFactory implements ExclusionFilterFactory {

	private RobotExclusionFilterFactory robotFactory = null;
	private String oracleUrl = null;
	private String accessGroup = null;
	
	public ObjectFilter<SearchResult> get() {
		OracleExclusionFilter filter = new OracleExclusionFilter(oracleUrl,
				accessGroup);
		if(robotFactory != null) {
			filter.setRobotFilter(robotFactory.get());
		}
		return filter;
	}

	public void shutdown() {
		// no-op... yet..
	}

	public RobotExclusionFilterFactory getRobotFactory() {
		return robotFactory;
	}

	public void setRobotFactory(RobotExclusionFilterFactory robotFactory) {
		this.robotFactory = robotFactory;
	}

	public String getOracleUrl() {
		return oracleUrl;
	}

	public void setOracleUrl(String oracleUrl) {
		this.oracleUrl = oracleUrl;
	}

	public String getAccessGroup() {
		return accessGroup;
	}

	public void setAccessGroup(String accessGroup) {
		this.accessGroup = accessGroup;
	}

}
