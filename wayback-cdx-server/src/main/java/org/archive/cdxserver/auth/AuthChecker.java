package org.archive.cdxserver.auth;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
    
    public final static String CDX_AUTH_TOKEN = "cdx-auth-token";
    
    protected String cookieAuthToken = CDX_AUTH_TOKEN;
	
	protected String accessCheckUrl;
	
	protected List<String> allUrlAccessTokens;
	protected List<String> allCdxFieldsAccessTokens;
	
	public AuthToken createAuthToken(HttpServletRequest request)
	{
	    return new AuthToken(this, extractAuthToken(request));
	}
	
    String extractAuthToken(HttpServletRequest request)
    {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieAuthToken)) {
                return cookie.getValue();
            }
        }

        return null;
    }
	
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
	
	public String getCookieAuthToken() {
        return cookieAuthToken;
    }

    public void setCookieAuthToken(String cookieAuthToken) {
        this.cookieAuthToken = cookieAuthToken;
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
