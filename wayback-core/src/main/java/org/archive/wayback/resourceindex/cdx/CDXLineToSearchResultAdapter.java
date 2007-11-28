/* CDXLineToSearchResultAdaptor
 *
 * $Id$
 *
 * Created on 2:27:16 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.cdx;


import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.Adapter;

/**
 * Adapter that converts a CDX record String into a SearchResult
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXLineToSearchResultAdapter implements Adapter<String,SearchResult> {

	public SearchResult adapt(String line) {
		return doAdapt(line);
	}
	/**
	 * @param line
	 * @return SearchResult representation of input line
	 */
	public static SearchResult doAdapt(String line) {
		SearchResult result = new SearchResult();
		String[] tokens = line.split(" ");
		if (tokens.length != 9) {
			return null;
			//throw new IllegalArgumentException("Need 9 columns("+line+")");
		}
		String url = tokens[0];
		String captureDate = tokens[1];
		String origHost = tokens[2];
		String mimeType = tokens[3];
		String httpResponseCode = tokens[4];
		String md5Fragment = tokens[5];
		String redirectUrl = tokens[6];
		long compressedOffset = -1;
		if(!tokens[7].equals("-")) {
			compressedOffset = Long.parseLong(tokens[7]);
		}
		String arcFileName = tokens[8];

		String origUrl = url;
		if(!url.startsWith(WaybackConstants.DNS_URL_PREFIX)) {
			try {
				UURI uri = UURIFactory.getInstance(
						WaybackConstants.HTTP_URL_PREFIX + url);
				origUrl = origHost + uri.getEscapedPathQuery();
			} catch (URIException e) {
				// TODO Stifle? throw an error?
				e.printStackTrace();
				return null;
			}
		}
		
		result.put(WaybackConstants.RESULT_URL, origUrl);
		result.put(WaybackConstants.RESULT_URL_KEY, url);
		result.put(WaybackConstants.RESULT_CAPTURE_DATE, captureDate);
		result.put(WaybackConstants.RESULT_ORIG_HOST, origHost);
		result.put(WaybackConstants.RESULT_MIME_TYPE, mimeType);
		result.put(WaybackConstants.RESULT_HTTP_CODE, httpResponseCode);
		result.put(WaybackConstants.RESULT_MD5_DIGEST, md5Fragment);
		result.put(WaybackConstants.RESULT_REDIRECT_URL, redirectUrl);
		// HACKHACK:
		result.put(WaybackConstants.RESULT_OFFSET, String.valueOf(compressedOffset));
		result.put(WaybackConstants.RESULT_ARC_FILE, arcFileName);

		return result;
	}
}
