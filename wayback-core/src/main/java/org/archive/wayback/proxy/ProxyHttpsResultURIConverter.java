package org.archive.wayback.proxy;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;

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
