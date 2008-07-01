/* CaptureSearchResult
 *
 * $Id$
 *
 * Created on 7:39:24 PM Jun 26, 2008.
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
package org.archive.wayback.core;

import java.util.Date;

import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureSearchResult extends SearchResult {
	
	private long cachedOffset = -1;
	private long cachedDate = -1;
	
	public static final String CAPTURE_ORIGINAL_URL = "originalurl";

	/**
	 * Result: canonicalized(lookup key) form of URL of captured document 
	 */
	public static final String CAPTURE_URL_KEY = "urlkey";
	
	/**
	 * Result: 14-digit timestamp when document was captured 
	 */
	public static final String CAPTURE_CAPTURE_TIMESTAMP = "capturetimestamp";

	/**
	 * Result: basename of ARC file containing this document.
	 */
	public static final String CAPTURE_FILE = "file";

	/**
	 * Result: compressed byte offset within ARC file where this document's
	 * gzip envelope begins. 
	 */
	public static final String CAPTURE_OFFSET = "compressedoffset";

	/**
	 * Result: compressed byte offset within ARC file where this document's
	 * gzip envelope Ends. 
	 */
	public static final String CAPTURE_END_OFFSET = "compressedendoffset";
	
	/**
	 * Result: best-guess at mime-type of this document.
	 */
	public static final String CAPTURE_MIME_TYPE = "mimetype";

	/**
	 * Result: 3-digit integer HTTP response code. may be '0' in some
	 * fringe conditions, old ARCs, bug in crawler, etc.
	 */
	public static final String CAPTURE_HTTP_CODE = "httpresponsecode";

	/**
	 * Result: some form of document fingerprint. This should represent the 
	 * HTTP payload only for HTTP captured resources. It may represent an MD5, a
	 * SHA1, and may be a fragment of the full representation of the digest.
	 */
	public static final String CAPTURE_DIGEST= "digest";

	/**
	 * Result: URL that this document redirected to, or '-' if it does
	 * not redirect
	 */
	public static final String CAPTURE_REDIRECT_URL = "redirecturl";

	/**
	 * Result: flag within a SearchResult that indicates this is the closest to
	 * a particular requested date.
	 */
	public static final String CAPTURE_CLOSEST_INDICATOR = "closest";
	public static final String CAPTURE_CLOSEST_VALUE = "true";

	/**
	 * Result: this key being present indicates that this particular capture
	 * was not actually stored, and that other values within this SearchResult
	 * are actually values from a different record which *should* be identical
	 * to this capture, had it been stored.
	 */
	public static final String CAPTURE_DUPLICATE_ANNOTATION = "duplicate";

	/**
	 * Result: this key is present when the CAPTURE_DUPLICATE_ANNOTATION is also
	 * present, with the value indicating the last date that was actually
	 * stored for this duplicate.
	 */
	public static final String CAPTURE_DUPLICATE_STORED_TS = "duplicate-ts";

	/**
	 * flag indicates that this document was downloaded and verified as 
	 * identical to a previous capture by digest.
	 */
	public static final String CAPTURE_DUPLICATE_DIGEST = "digest";

	/**
	 * flag indicates that this document was NOT downloaded, but that the
	 * origin server indicated that the document had not changed, based on
	 * If-Modified HTTP request headers.
	 */
	public static final String CAPTURE_DUPLICATE_HTTP = "http";
	public String getOriginalUrl() {
		return get(CAPTURE_ORIGINAL_URL);
	}
	public void setOriginalUrl(String originalUrl) {
		put(CAPTURE_ORIGINAL_URL,originalUrl);
	}
	public String getOriginalHost() {
		return UrlOperations.urlToHost(getOriginalUrl());
	}
	public String getUrlKey() {
		return get(CAPTURE_URL_KEY);
	}
	public void setUrlKey(String urlKey) {
		put(CAPTURE_URL_KEY,urlKey);
	}
	public Date getCaptureDate() {
		if(cachedDate == -1) {
			cachedDate = tsToDate(getCaptureTimestamp()).getTime();
		}
		return new Date(cachedDate);
	}
	public void setCaptureDate(Date date) {
		cachedDate = date.getTime();
		put(CAPTURE_CAPTURE_TIMESTAMP, dateToTS(date));
	}
	public String getCaptureTimestamp() {
		return get(CAPTURE_CAPTURE_TIMESTAMP);
	}
	public void setCaptureTimestamp(String timestamp) {
		put(CAPTURE_CAPTURE_TIMESTAMP,timestamp);
	}
	public String getFile() {
		return get(CAPTURE_FILE);
	}
	public void setFile(String file) {
		put(CAPTURE_FILE, file);
	}
	public long getOffset() {
		if(cachedOffset == -1) {
			cachedOffset = Long.parseLong(get(CAPTURE_OFFSET));
		}
		return cachedOffset;
	}
	public void setOffset(long offset) {
		cachedOffset = offset;
		put(CAPTURE_OFFSET,String.valueOf(offset));
	}
	public String getMimeType() {
		return get(CAPTURE_MIME_TYPE);
	}
	public void setMimeType(String mimeType) {
		put(CAPTURE_MIME_TYPE,mimeType);
	}
	public String getHttpCode() {
		return get(CAPTURE_HTTP_CODE);
	}
	public void setHttpCode(String httpCode) {
		put(CAPTURE_HTTP_CODE,httpCode);
	}
	public String getDigest() {
		return get(CAPTURE_DIGEST);
	}
	public void setDigest(String digest) {
		put(CAPTURE_DIGEST,digest);
	}
	public String getRedirectUrl() {
		return get(CAPTURE_REDIRECT_URL);
	}
	public void setRedirectUrl(String url) {
		put(CAPTURE_REDIRECT_URL,url);
	}
	public boolean isClosest() {
		return getBoolean(CAPTURE_CLOSEST_INDICATOR);
	}
	public void setClosest(boolean value) {
		putBoolean(CAPTURE_CLOSEST_INDICATOR,value);
	}
	public void flagDuplicateDigest(Date storedDate) {
		put(CAPTURE_DUPLICATE_ANNOTATION,CAPTURE_DUPLICATE_DIGEST);
		put(CAPTURE_DUPLICATE_STORED_TS,dateToTS(storedDate));
	}
	public void flagDuplicateDigest(String storedTS) {
		put(CAPTURE_DUPLICATE_ANNOTATION,CAPTURE_DUPLICATE_DIGEST);
		put(CAPTURE_DUPLICATE_STORED_TS,storedTS);
	}
	public boolean isDuplicateDigest() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		return (dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_DIGEST));
	}
	public Date getDuplicateDigestStoredDate() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		Date date = null;
		if(dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_DIGEST)) {
			date = tsToDate(get(CAPTURE_DUPLICATE_STORED_TS));
		}
		return date;
	}
	public String getDuplicateDigestStoredTimestamp() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		String ts = null;
		if(dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_DIGEST)) {
			ts = get(CAPTURE_DUPLICATE_STORED_TS);
		}
		return ts;
	}
}
