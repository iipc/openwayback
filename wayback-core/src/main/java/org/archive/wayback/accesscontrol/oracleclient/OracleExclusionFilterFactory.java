package org.archive.wayback.accesscontrol.oracleclient;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class OracleExclusionFilterFactory implements ExclusionFilterFactory {

	private String oracleUrl = null;
	private String accessGroup = null;
	
	public ObjectFilter<CaptureSearchResult> get() {
		OracleExclusionFilter filter = new OracleExclusionFilter(oracleUrl,
				accessGroup);
		return filter;
	}

	public void shutdown() {
		// no-op... yet..
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
