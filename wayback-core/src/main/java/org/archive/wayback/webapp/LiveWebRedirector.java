package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

public class LiveWebRedirector {
	
	enum RedirectType {
		NONE,
		ALL,
		EMBEDS_ONLY,
	};
	
	enum LiveWebState {
		NOT_FOUND,
		FOUND,
		REDIRECTED
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
		props.setProperty("404", LiveWebRedirector.RedirectType.ALL.name());
		this.setStatusLiveWebPolicy(props);
	}

	/**
	 * Check the statusLiveWebType to see if, given the WaybackExceptions
	 * status code, should redirect ALL, NONE or EMBEDS_ONLY
	 * 
	 * Before redirecting, will always check with liveweb to see if it returns a 200 request,
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
	public LiveWebState handleRedirect(WaybackException e, WaybackRequest wbRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
	{
		if (statusLiveWebPolicy == null) {
			return LiveWebState.NOT_FOUND;
		}
		
		// Don't do any redirect for identity context or if no handler is set
		if ((wbRequest == null) || wbRequest.isIdentityContext() || (liveWebHandler == null)) {
			return LiveWebState.NOT_FOUND;
		}
		
		int status = e.getStatus();
		
		String stateName = statusLiveWebPolicy.getProperty(String.valueOf(status));
		
		if (stateName == null) {
			stateName = statusLiveWebPolicy.getProperty(DEFAULT);
		}
		
		RedirectType state = RedirectType.ALL;
		
		if (stateName != null) {
			state = RedirectType.valueOf(stateName);
		}
		
		String redirUrl = null;
		
		if (state == RedirectType.NONE) {
			return LiveWebState.NOT_FOUND;
		}
		
		redirUrl = liveWebHandler.getLiveWebRedirect(httpRequest, wbRequest, e);
		
		// Don't redirect if redirUrl null
		if (redirUrl == null) {
			return LiveWebState.NOT_FOUND;
		}
		
		// If embeds_only and not embed return if it was found		
		if (state == RedirectType.EMBEDS_ONLY) {
			boolean allowRedirect = wbRequest.isAnyEmbeddedContext();
			
			if (!allowRedirect) {
				String referrer = wbRequest.getRefererUrl();
				String replayPrefix = wbRequest.getAccessPoint().getReplayPrefix();
				
				if ((referrer != null) && (replayPrefix != null) && referrer.startsWith(replayPrefix)) {
					allowRedirect = true;
				}
			}
			
			if (!allowRedirect) {
				return LiveWebState.FOUND;
			}
		}
		
		// Now try to do a redirect
				
		// If set to DEFAULT then compute the standard redir url
		if (redirUrl.equals(DEFAULT)) {
			redirUrl = getLiveWebPrefix() + wbRequest.getRequestUrl();
		}
		
		httpResponse.sendRedirect(redirUrl);
		return LiveWebState.REDIRECTED;
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
