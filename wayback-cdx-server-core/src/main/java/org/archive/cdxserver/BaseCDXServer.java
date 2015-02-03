package org.archive.cdxserver;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.cdxserver.auth.AllAccessAuth;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.url.UrlSurtRangeComputer;
import org.archive.url.WaybackURLKeyMaker;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;

@Controller
public class BaseCDXServer implements InitializingBean {
	
	public final static String CDX_AUTH_TOKEN = "cdx_auth_token";

	protected String cookieAuthToken = CDX_AUTH_TOKEN;
	
	protected UrlSurtRangeComputer urlSurtRangeComputer;
	protected WaybackURLKeyMaker canonicalizer = null;
	protected AuthChecker authChecker;
	protected String ajaxAccessControl;
	
	protected boolean surtMode = false;

	public boolean isSurtMode() {
		return surtMode;
	}

	public void setSurtMode(boolean surtMode) {
		this.surtMode = surtMode;
	}
	
	public String getCookieAuthToken() {
		return cookieAuthToken;
	}

	public void setCookieAuthToken(String cookieAuthToken) {
		this.cookieAuthToken = cookieAuthToken;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (authChecker == null) {
			authChecker = new AllAccessAuth();
		}
		
		canonicalizer = new WaybackURLKeyMaker(surtMode);
		urlSurtRangeComputer = new UrlSurtRangeComputer(surtMode);
	}
	
	public String canonicalize(String url, boolean surt) throws UnsupportedEncodingException, URISyntaxException
	{
		if ((canonicalizer == null) || (url == null) || url.isEmpty()) {
			return url;
		}
		
		url = java.net.URLDecoder.decode(url, "UTF-8");
		
		if (surt) {
			return url;
		}
		
		int slashIndex = url.indexOf('/');
		// If true, assume this is already a SURT and skip
		if ((slashIndex > 0) && url.charAt(slashIndex - 1) == ')') {
			return url;
		}
						
		return canonicalizer.makeKey(url);
	}
	
	protected void prepareResponse(HttpServletResponse response)
	{
		response.setContentType("text/plain; charset=\"UTF-8\"");
	}
	
	protected void handleAjax(HttpServletRequest request, HttpServletResponse response)
	{
	    String origin = request.getHeader("Origin");
	    
	    if (origin == null) {
	        return;
	    }
	    
	    response.setHeader("Access-Control-Allow-Credentials", "true");
	    response.setHeader("Access-Control-Allow-Origin", origin);
	}
		
	public AuthChecker getAuthChecker() {
		return authChecker;
	}

	public void setAuthChecker(AuthChecker authChecker) {
		this.authChecker = authChecker;
	}

    public String getAjaxAccessControl() {
        return ajaxAccessControl;
    }

    public void setAjaxAccessControl(String ajaxAccessControl) {
        this.ajaxAccessControl = ajaxAccessControl;
    }
    
    protected AuthToken createAuthToken(HttpServletRequest request)
    {
    	return new AuthToken(extractAuthToken(request, cookieAuthToken));
    }
    
    protected String extractAuthToken(HttpServletRequest request, String cookieAuthToken) {
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
}