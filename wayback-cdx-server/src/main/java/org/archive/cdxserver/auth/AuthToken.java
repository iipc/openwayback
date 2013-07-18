package org.archive.cdxserver.auth;


public class AuthToken {
	
	final String authToken;
	final private AuthChecker authChecker;
	
	private Boolean allUrlsAllowed;
	private Boolean allCdxAllowed;
	
	public static AuthToken createAllAccessToken()
	{
	    return new AuthToken(null, null);
	}
	
	AuthToken(AuthChecker authChecker, String authToken)
	{
		this.authChecker = authChecker;
		this.authToken = authToken;
	}
	
	public boolean isAllUrlAccessAllowed()
	{
		if (authChecker == null) {
			return true;
		}
		
		if (this.allUrlsAllowed == null) {
			this.allUrlsAllowed = authChecker.isAllUrlAccessAllowed(this);	
		}
		
		return this.allUrlsAllowed;
	}
	
	public boolean isAllCdxFieldsAllowed()
	{
		if (authChecker == null) {
			return true;
		}
		
		if (this.allCdxAllowed == null) {
			this.allCdxAllowed = authChecker.isAllCdxAccessAllowed(this);
		}
		
		return this.allCdxAllowed;
	}
}
