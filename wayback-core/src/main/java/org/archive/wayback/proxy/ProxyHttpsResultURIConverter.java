package org.archive.wayback.proxy;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;

/**
 * {@link ResultURIConverter} that replaces "{@code https://}" with "{@code http://}".
 * <p>For use in proxy-mode, where all access need to be plain HTTP.</p>
 *
 */
public class ProxyHttpsResultURIConverter implements ResultURIConverter {

	@Override
	public String makeReplayURI(String datespec, String url) {
		
		if (url.startsWith(WaybackConstants.HTTPS_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url.substring(WaybackConstants.HTTPS_URL_PREFIX.length());
		} else if (!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
			url = WaybackConstants.HTTP_URL_PREFIX + url;
		}
		
		return url;
	}
}
