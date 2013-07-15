package org.archive.cdxserver.auth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Simple checking of permissions for cdx server actions
 * Permissions include:
 *  -Ability to see blocked urls
 *  -Ability to see full cdx line
 *  
 * @author ilya
 *
 */
public class AuthChecker {
	
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

	public boolean checkAccess(String url)
	{
		if (accessCheckUrl == null) {
			return true;
		}
		
		InputStream is = null;
		
		try {
			is = new URL(accessCheckUrl + url).openStream();
			String result = IOUtils.toString(is);
			
			if (result.contains("allow")) {
				return true;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} finally {
			if (is != null) {
				IOUtils.closeQuietly(is);
			}
		}
		
		return false;
	}

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
