package org.archive.cdxserver.auth;

import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.format.cdx.FieldSplitFormat;

public class AllAccessAuth implements AuthChecker {

	@Override
    public boolean isAllUrlAccessAllowed(AuthToken auth) {
		return true;
    }

	@Override
    public boolean isAllCdxFieldAccessAllowed(AuthToken auth) {
		return true;
    }

	@Override
    public String getPublicCdxFields() {
		return null;
    }
	
	@Override
    public FieldSplitFormat getPublicCdxFormat() {
		return null;
    }

	@Override
    public CDXAccessFilter createAccessFilter(AuthToken auth) {
		return null;
    }
}
