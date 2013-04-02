package org.archive.wayback.webapp;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

public abstract class LiveWebRequestHandler extends AbstractRequestHandler {

	// If this resource has been successfully handled by the liveweb, return the redirect URL
	// Otherwise return null
	public abstract String getLiveWebRedirect(HttpServletRequest request, WaybackRequest wbRequest);
}
