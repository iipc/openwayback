package org.archive.wayback.webapp;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

public interface LiveWebRedirector {
	
	public final static String DEFAULT = "default";
	
	public enum RedirectType {
		NONE,
		ALL,
		EMBEDS_ONLY,
	};
	
	public enum LiveWebState {
		NOT_FOUND,
		FOUND,
		REDIRECTED
	};
	
	public LiveWebState handleRedirect(WaybackException e, WaybackRequest wbRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException;
	public String getLiveWebPrefix();
}
