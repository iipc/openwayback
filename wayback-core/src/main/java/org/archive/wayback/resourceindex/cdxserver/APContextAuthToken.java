package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.auth.AuthToken;
import org.archive.wayback.webapp.AccessPoint;

public class APContextAuthToken extends AuthToken {
	final AccessPoint ap;
	
	public APContextAuthToken(AccessPoint ap)
	{
		this.ap = ap;
	}
}
