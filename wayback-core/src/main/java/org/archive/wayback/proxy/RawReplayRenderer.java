/* ReplayRenderer
 *
 * $Id$
 *
 * Created on 5:50:38 PM Oct 31, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.proxy;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.replay.BaseReplayRenderer;
import org.archive.wayback.util.UrlCanonicalizer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class RawReplayRenderer extends BaseReplayRenderer {

	private final static String HTTP_LOCATION_HEADER = "Location";

	/**
	 * Convert a "Location" header only.
	 * 
	 * @param key
	 * @param value
	 * @param uriConverter
	 * @param result
	 * @return String
	 */
	protected String filterHeader(final String key, final String value,
			final ResultURIConverter uriConverter, SearchResult result) {
		String finalHeaderValue = value;
		String keyUp = key.toUpperCase();
		if (0 == keyUp.indexOf(HTTP_LOCATION_HEADER.toUpperCase())) {
			String baseUrl = result.getAbsoluteUrl();
			String captureDate = result.getCaptureDate();
			String url = UrlCanonicalizer.resolveUrl(baseUrl, value);
			return uriConverter.makeReplayURI(captureDate,url);
		}
		return finalHeaderValue;
	}
}
