package org.archive.cdxserver.auth;


public class AuthToken {
	
	final String authToken;
	Boolean cachedAllUrlAllow = null;
	Boolean cachedAllCdxAllow = null;
	boolean ignoreRobots = false;
	
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

	public boolean isIgnoreRobots() {
		return ignoreRobots;
	}

	public void setIgnoreRobots(boolean ignoreRobots) {
		this.ignoreRobots = ignoreRobots;
	}
}
