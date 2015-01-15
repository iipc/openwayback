package org.archive.wayback.accesscontrol.oracleclient;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class CustomPolicyOracleFilterFactory extends
		OracleExclusionFilterFactory {

	@Override
	public ExclusionFilter get() {
		return new CustomPolicyOracleFilter(this.getOracleUrl(),
			this.getAccessGroup(), this.getProxyHostPort());
	}
}
