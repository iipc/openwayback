package org.archive.wayback.proxy;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.IdentityResultURIConverterFactory;

/**
 * {@link ResultURIConverter} that replaces "{@code https://}" with "{@code http://}".
 * <p>For use in proxy-mode, where all access need to be plain HTTP.</p>
 *
 * This class also implements {@link ContextResultURIConverterFactory} to save wrapping
 * this object with {@link IdentityResultURIConverterFactory}.
 */
public class ProxyHttpsResultURIConverter implements ResultURIConverter, ContextResultURIConverterFactory {

	@Override
	public String makeReplayURI(String datespec, String url) {
		
		if (url.startsWith(WaybackConstants.HTTPS_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url.substring(WaybackConstants.HTTPS_URL_PREFIX.length());
		} else if (!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url;
		}
		
		return url;
	}
	@Override
	public ResultURIConverter getContextConverter(String flags) {
		return this;
	}
}
