package org.archive.cdxserver.auth;

import org.archive.format.cdx.CDXLine;

public class AllAccessAuth implements AuthChecker {

	@Override
    public boolean isAllUrlAccessAllowed(AuthToken auth) {
		return true;
    }

	@Override
    public boolean isUrlAllowed(String url, AuthToken auth) {
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
    public boolean isCaptureAllowed(CDXLine line, AuthToken auth) {
		return true;
    }
}
