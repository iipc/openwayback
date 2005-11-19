/* CDXRecord
 *
 * $Id$
 *
 * Created on 4:40:45 PM Nov 10, 2005.
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
package org.archive.wayback.cdx;

import java.text.ParseException;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXRecord {
    public final static String CDX_HEADER_MAGIC = " CDX N b h m s k r V g";

    public String url;

	public String captureDate;

	public String origHost = null;

	public String mimeType = null;

	public String httpResponseCode = null;

	public String md5Fragment = null;

	public String redirectUrl = null;

	public long compressedOffset = -1;

	public String arcFileName = null;

	public CDXRecord() {
		super();
	}

	
	/**
	 * Attempt to deserialize state from a single text line, fields delimited by
	 * spaces. There are standard ways to do this, and this is not one of
	 * them... for no good reason.
	 *
	 * @param line
	 * @param lineNumber
	 * @throws ParseException
	 */
	public void parseLine(final String line, final int lineNumber)
			throws ParseException {
		String[] tokens = line.split(" ");
		if (tokens.length != 9) {
			throw new ParseException(line, lineNumber);
		}
		url = tokens[0];
		captureDate = tokens[1];
		origHost = tokens[2];
		mimeType = tokens[3];
		httpResponseCode = tokens[4];
		md5Fragment = tokens[5];
		redirectUrl = tokens[6];
		compressedOffset = Long.parseLong(tokens[7]);
		arcFileName = tokens[8];
	}

	public SearchResult toSearchResult() {
		SearchResult result = new SearchResult();

		result.put(WaybackConstants.RESULT_URL, url);
		result.put(WaybackConstants.RESULT_CAPTURE_DATE, captureDate);
		result.put(WaybackConstants.RESULT_ORIG_HOST, origHost);
		result.put(WaybackConstants.RESULT_MIME_TYPE, mimeType);
		result.put(WaybackConstants.RESULT_HTTP_CODE, httpResponseCode);
		result.put(WaybackConstants.RESULT_MD5_DIGEST, md5Fragment);
		result.put(WaybackConstants.RESULT_REDIRECT_URL, redirectUrl);
		// HACKHACK:
		result.put(WaybackConstants.RESULT_OFFSET, "" + compressedOffset);
		result.put(WaybackConstants.RESULT_ARC_FILE, arcFileName);

		return result;
	}

	public void fromSearchResult(final SearchResult result) {
		url = result.get(WaybackConstants.RESULT_URL);
		captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		origHost = result.get(WaybackConstants.RESULT_ORIG_HOST);
		mimeType = result.get(WaybackConstants.RESULT_MIME_TYPE);
		httpResponseCode = result.get(WaybackConstants.RESULT_HTTP_CODE);
		md5Fragment = result.get(WaybackConstants.RESULT_MD5_DIGEST);
		redirectUrl = result.get(WaybackConstants.RESULT_REDIRECT_URL);
		compressedOffset = Long.parseLong(result.get(
				WaybackConstants.RESULT_OFFSET));
		arcFileName = result.get(WaybackConstants.RESULT_ARC_FILE);
	}

	public String toValue() {
		return url + " " + captureDate + " " + origHost + " " + mimeType + " "
				+ httpResponseCode + " " + md5Fragment + " " + redirectUrl
				+ " " + compressedOffset + " " + arcFileName;
	}

	public String toKey() {
		return url + " " + captureDate;
	}
}
