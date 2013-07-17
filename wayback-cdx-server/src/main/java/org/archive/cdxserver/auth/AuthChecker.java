package org.archive.cdxserver.auth;

import java.util.List;

/**
 * Simple checking of permissions for cdx server actions
 * Permissions include:
 *  -Ability to see blocked urls
 *  -Ability to see full cdx line
 *  
 *  The checkAccess() for each url is implemented in the subclasses 
 *  
 * @author ilya
 *
 */
public abstract class AuthChecker {
	
	protected String accessCheckUrl;
	
	protected List<String> allUrlAccessTokens;
	protected List<String> allCdxFieldsAccessTokens;
	
	boolean isAllowed(AuthToken auth, List<String> allowVector)
	{
		if (auth == null || auth.authToken == null || allowVector == null) {
			return false;
		}
		
		if (allowVector.contains(auth.authToken)) {
			return true;
		}
		
		return false;
	}
	
	public boolean isAllUrlAccessAllowed(AuthToken auth)
	{
		return isAllowed(auth, allUrlAccessTokens);
	}
	
	public boolean isAllCdxAccessAllowed(AuthToken auth)
	{
		return isAllowed(auth, allCdxFieldsAccessTokens);
	}

	public abstract boolean checkAccess(String url);

	public String getAccessCheckUrl() {
		return accessCheckUrl;
	}

	public void setAccessCheckUrl(String accessCheckUrl) {
		this.accessCheckUrl = accessCheckUrl;
	}
	
	public List<String> getAllUrlAccessTokens() {
		return allUrlAccessTokens;
	}

	public void setAllUrlAccessTokens(List<String> allUrlAccessTokens) {
		this.allUrlAccessTokens = allUrlAccessTokens;
	}

	public List<String> getAllCdxFieldsAccessTokens() {
		return allCdxFieldsAccessTokens;
	}

	public void setAllCdxFieldsAccessTokens(List<String> allCdxFieldsAccessTokens) {
		this.allCdxFieldsAccessTokens = allCdxFieldsAccessTokens;
	}
	
}
