package org.archive.cdxserver.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class AuthToken {

	public final static String CDX_AUTH_TOKEN = "cdx-auth-token";
	
	final String authToken;
	final private AuthChecker authChecker;
	
	private Boolean allUrlsAllowed;
	private Boolean allCdxAllowed;
	
	public AuthToken(AuthChecker authChecker, HttpServletRequest request)
	{
		this.authChecker = authChecker;
		
		if (authChecker != null) {
			this.authToken = extractAuthToken(request);
		} else {
			this.authToken = null;
		}
	}
	
	private String extractAuthToken(HttpServletRequest request)
	{
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null) {
			return null;
		}
		
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(CDX_AUTH_TOKEN)) {
				return cookie.getValue();
			}
		}
		
		return null;
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
