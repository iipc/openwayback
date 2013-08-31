package org.archive.cdxserver.auth;

import org.archive.format.cdx.CDXLine;

public interface AuthChecker {
	public boolean isAllUrlAccessAllowed(AuthToken auth);
	public boolean isUrlAllowed(String url, AuthToken auth);

	public boolean isAllCdxFieldAccessAllowed(AuthToken auth);
	public String getPublicCdxFields();
	
	public boolean isCaptureAllowed(CDXLine line, AuthToken auth);
}
