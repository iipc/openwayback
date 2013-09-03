package org.archive.cdxserver.auth;

import org.archive.cdxserver.filter.CDXAccessFilter;

public interface AuthChecker {
	
	public CDXAccessFilter createAccessFilter(AuthToken auth);
	
	public boolean isAllUrlAccessAllowed(AuthToken auth);
	//public boolean isUrlAllowed(String url, AuthToken auth);

	public boolean isAllCdxFieldAccessAllowed(AuthToken auth);
	public String getPublicCdxFields();
	
	//public boolean isCaptureAllowed(CDXLine line, AuthToken auth);
}
