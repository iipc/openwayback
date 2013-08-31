package org.archive.cdxserver.auth;


public class AuthToken {
	
	final String authToken;
	Boolean cachedAllUrlAllow = null;
	Boolean cachedAllCdxAllow = null;
	
	public AuthToken()
	{
		this.authToken = null;
	}
	
	public AuthToken(String authToken)
	{
		this.authToken = authToken;
	}
	
	//TODO: is this good way of doing so
	public void setAllCdxFieldsAllow()
	{
		cachedAllCdxAllow = true;
	}
}
