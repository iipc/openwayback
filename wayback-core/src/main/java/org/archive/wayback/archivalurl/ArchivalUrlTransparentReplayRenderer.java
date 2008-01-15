package org.archive.wayback.archivalurl;

import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.replay.TransparentReplayRenderer;
import org.archive.wayback.util.url.UrlOperations;

/**
 * Slight extension to TransparentReplayRenderer, which rewrites Location and
 * Content-Base HTTP headers as they go out.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlTransparentReplayRenderer 
extends TransparentReplayRenderer {

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HeaderFilter#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.SearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, SearchResult result) {

		String keyUp = key.toUpperCase();

		// rewrite Location header URLs
		if (keyUp.startsWith(HTTP_LOCATION_HEADER_UP) ||
				keyUp.startsWith(HTTP_CONTENT_BASE_HEADER_UP)) {

			String baseUrl = result.getAbsoluteUrl();
			String cd = result.getCaptureDate();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);

			output.put(key, uriConverter.makeReplayURI(cd,u));

		} else {
			// others go out as-is:

			output.put(key, value);
		}
	}
}
