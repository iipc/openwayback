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

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXRecord {
	/**
	 * CDX Header line for these fields. not very configurable..
	 */
	public final static String CDX_HEADER_MAGIC = " CDX N b h m s k r V g";

	/**
	 * capture url for this document
	 */
	public String url;

	/**
	 * date this document was captured, 14-digit
	 */
	public String captureDate;

	/**
	 * original host for this document, URL may have been massaged
	 */
	public String origHost = null;

	/**
	 * guessed mime-type of this document
	 */
	public String mimeType = null;

	/**
	 * HTTP response code for this document
	 */
	public String httpResponseCode = null;

	/**
	 * Digest for this document -- very unclear what it means, is MD5?, partial
	 * MD5?, includes HTTP headers?...
	 */
	public String md5Fragment = null;

	/**
	 * URL that this document redirected to
	 */
	public String redirectUrl = null;

	/**
	 * compressed offset within the ARC file where this document begins
	 */
	public long compressedOffset = -1;

	/**
	 * name of ARC file containing this document, may or may not include .arc.gz
	 */
	public String arcFileName = null;

	/**
	 * Constructor
	 */
	public CDXRecord() {
		super();
	}

	/**
	 * return the canonical string key for the URL argument.
	 * 
	 * @param urlString
	 * @return String lookup key for URL argument.
	 * @throws URIException 
	 */
	public static String urlStringToKey(final String urlString)
			throws URIException {

		String searchUrl = urlString;

		// TODO: this will only work with http:// scheme. should work with all?
		// force add of scheme and possible add '/' with empty path:
		if (searchUrl.startsWith("http://")) {
			if (-1 == searchUrl.indexOf('/', 8)) {
				searchUrl = searchUrl + "/";
			}
		} else {
			if (-1 == searchUrl.indexOf("/")) {
				searchUrl = searchUrl + "/";
			}
			searchUrl = "http://" + searchUrl;
		}

		// convert to UURI to perform require URI fixup:
		UURI searchURI = UURIFactory.getInstance(searchUrl);

		// replace ' ' with '+' (this is only to match Alexa's canonicalization)
		searchURI.setPath(searchURI.getPath().replace(' ', '+'));
		return searchURI.getHostBasename() + searchURI.getEscapedPathQuery();
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

	/**
	 * @return SearchResult with values of this CDXRecord
	 */
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

	/** Initialize this CDXRecord values from a SearchResult
	 * @param result
	 */
	public void fromSearchResult(final SearchResult result) {
		url = result.get(WaybackConstants.RESULT_URL);
		captureDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		origHost = result.get(WaybackConstants.RESULT_ORIG_HOST);
		mimeType = result.get(WaybackConstants.RESULT_MIME_TYPE);
		httpResponseCode = result.get(WaybackConstants.RESULT_HTTP_CODE);
		md5Fragment = result.get(WaybackConstants.RESULT_MD5_DIGEST);
		redirectUrl = result.get(WaybackConstants.RESULT_REDIRECT_URL);
		compressedOffset = Long.parseLong(result
				.get(WaybackConstants.RESULT_OFFSET));
		arcFileName = result.get(WaybackConstants.RESULT_ARC_FILE);
	}

	/**
	 * @return a BDBJE value for this record
	 */
	public String toValue() {
		return url + " " + captureDate + " " + origHost + " " + mimeType + " "
				+ httpResponseCode + " " + md5Fragment + " " + redirectUrl
				+ " " + compressedOffset + " " + arcFileName;
	}

	/**
	 * @return a BDBJE key for this record
	 */
	public String toKey() {
		return url + " " + captureDate;
	}
}
