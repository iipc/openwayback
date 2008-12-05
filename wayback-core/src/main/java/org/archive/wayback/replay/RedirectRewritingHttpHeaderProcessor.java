/* RedirectRewritingHttpHeaderProcessor
 *
 * $Id$
 *
 * Created on 3:00:48 PM Jul 15, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.replay;

import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RedirectRewritingHttpHeaderProcessor 
	implements HttpHeaderProcessor {

	private static String DEFAULT_PREFIX = null;
	private String prefix = DEFAULT_PREFIX; 

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HttpHeaderProcessor#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.CaptureSearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {

		String keyUp = key.toUpperCase();

		// first stick it in as-is, or with prefix, then maybe we'll overwrite
		// with the later logic.
		if(prefix == null) {
			output.put(key, value);
		} else {
			output.put(prefix + key, value);
		}

		// rewrite Location header URLs
		if (keyUp.startsWith(HTTP_LOCATION_HEADER_UP) ||
			keyUp.startsWith(HTTP_CONTENT_LOCATION_HEADER_UP) ||
			keyUp.startsWith(HTTP_CONTENT_BASE_HEADER_UP)) {

			String baseUrl = result.getOriginalUrl();
			String cd = result.getCaptureTimestamp();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);

			output.put(key, uriConverter.makeReplayURI(cd,u));

		} else if(keyUp.startsWith(HTTP_CONTENT_TYPE_HEADER_UP)) {
			// let's leave this one as-is:
			output.put(key,value);
		}
	}
}
