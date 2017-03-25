package org.archive.wayback.instantliveweb;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.webapp.DefaultLiveWebRedirector;

public class InstaLiveWebRedirector extends DefaultLiveWebRedirector {

	@Override
    public LiveWebState handleRedirect(WaybackException e,
            WaybackRequest wbRequest, HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {

		if ((wbRequest == null) || wbRequest.isIdentityContext() || (liveWebHandler == null)) {
			return LiveWebState.NOT_FOUND;
		}
		
		if (e.getStatus() != 404) {
			return LiveWebState.NOT_FOUND;
		}
		
		String redirUrl = liveWebHandler.getLiveWebRedirect(httpRequest, wbRequest, e);
		
		// Don't redirect if redirUrl null
		if (redirUrl == null) {
			return LiveWebState.NOT_FOUND;
		}
				
//		if (redirUrl.equals(DEFAULT)) {
//			redirUrl = getLiveWebPrefix() + wbRequest.getRequestUrl();
//		}
		
		if (wbRequest.isAnyEmbeddedContext() || wbRequest.isAjaxRequest()) {
			httpResponse.sendRedirect(redirUrl);
			return LiveWebState.REDIRECTED;
		} else {
			return LiveWebState.FOUND;
		}
    }
}
