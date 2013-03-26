package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

public class LiveWebRedirector {
	
	enum RedirectState {
		NEVER,
		ALWAYS,
		IF_FOUND,
	};
	
	public final static String DEFAULT = "default";
	
	protected String liveWebPrefix;
	
	protected Properties statusLiveWebPolicy;
	
	protected LiveWebRequestHandler liveWebHandler;
	
	public LiveWebRedirector() {
		
	}
	
	public LiveWebRedirector(String liveWebPrefix) {
		this.setLiveWebPrefix(liveWebPrefix);
		
		// Default to always redirect for 404 error, but not others
		Properties props = new Properties();
		props.setProperty("404", LiveWebRedirector.RedirectState.ALWAYS.name());
		this.setStatusLiveWebPolicy(props);
	}

	/**
	 * Check the statusLivewebPolicy to see if, given the WaybackExceptions
	 * status code, should ALWAYS redirect, NEVER redirect, or CHECK_IF_SUCCESS
	 * first before redirecting to liveweb.
	 * 
	 * The IF_FOUND will GET liveweb to see if it returns a 200 request,
	 * then redirect to same request, resulting in 2 checks to liveweb
	 * 
	 * "default" property is checked if no property is found for current access code
	 *  or for other exceptions
	 * 
	 * If default property is missing, default is to not redirect
	 * 
	 * @param e
	 * @throws IOException 
	 */
	public boolean handleRedirect(WaybackException e, WaybackRequest wbRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
	{
		if (statusLiveWebPolicy == null) {
			return false;
		}
		
		int status = e.getStatus();
		
		String stateName = statusLiveWebPolicy.getProperty(String.valueOf(status));
		
		if (stateName == null) {
			stateName = statusLiveWebPolicy.getProperty(DEFAULT);
		}
		
		RedirectState state = RedirectState.NEVER;
		
		if (stateName != null) {
			state = RedirectState.valueOf(stateName);
		}
				
		switch (state) {
		case NEVER:
			return false;
			
		case ALWAYS:
			break;
			
		case IF_FOUND:
			if (liveWebHandler == null) {
				return false;
			}
			
			// Redirect only if found by liveweb handler
			if (!liveWebHandler.isLiveWebFound(httpRequest, wbRequest)) {
				return false;
			}
			break;
			
		default:
			return false;
		}
		
		// Redirect to LiveWeb!		
		String liveUrl = getLiveWebPrefix() + wbRequest.getRequestUrl();
		httpResponse.sendRedirect(liveUrl);
		return true;
	}

	public String getLiveWebPrefix() {
		return liveWebPrefix;
	}

	public void setLiveWebPrefix(String liveWebPrefix) {
		this.liveWebPrefix = liveWebPrefix;
	}

	public Properties getStatusLiveWebPolicy() {
		return statusLiveWebPolicy;
	}

	public void setStatusLiveWebPolicy(Properties statusLiveWebPolicy) {
		this.statusLiveWebPolicy = statusLiveWebPolicy;
	}

	public LiveWebRequestHandler getLiveWebHandler() {
		return liveWebHandler;
	}

	public void setLiveWebHandler(LiveWebRequestHandler liveWebHandler) {
		this.liveWebHandler = liveWebHandler;
	}
}
