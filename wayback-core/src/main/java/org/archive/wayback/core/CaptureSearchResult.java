/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
	private long cachedCompressedLength = -1;
	private long cachedDate = -1;
	
	// Keep track of the z matched result so that we can walk
	// back if the current result is a self-redirect/otherwise unavailable
	private CaptureSearchResult prevResult = null;
	private CaptureSearchResult nextResult = null;
	
	public static final String CAPTURE_ORIGINAL_URL = "url";
	public static final String CAPTURE_ORIGINAL_HOST = "host";

	/**
	 * Result: canonicalized(lookup key) form of URL of captured document 
	 */
	public static final String CAPTURE_URL_KEY = "urlkey";
	
	/**
	 * Result: 14-digit timestamp when document was captured 
	 */
	public static final String CAPTURE_CAPTURE_TIMESTAMP = "capturedate";

	/**
	 * Result: basename of ARC/WARC file containing this document.
	 */
	public static final String CAPTURE_FILE = "file";

	/**
	 * Result: compressed byte offset within ARC/WARC file where this document's
	 * gzip envelope begins. 
	 */
	public static final String CAPTURE_OFFSET = "compressedoffset";

	/**
	 * Result: compressed byte offset within ARC/WARC file where this document's
	 * gzip envelope Ends.
	 */
	public static final String CAPTURE_COMPRESSED_LENGTH = "compressedendoffset";
	
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
	 * Result: String flags which indicate robot instructions found in an HTML
	 * page. Currently one or more of:
	 * <li>"A" - noarchive</li>
	 * <li>"F" - nofollow</li>
	 * <li>"I" - noindex</li>
	 * @see "http://noarchive.net/"
	 */
	public static final String CAPTURE_ROBOT_FLAGS = "robotflags";
	
	public static final String CAPTURE_ROBOT_NOARCHIVE = "A";
	public static final String CAPTURE_ROBOT_NOFOLLOW = "F";
	public static final String CAPTURE_ROBOT_NOINDEX = "I";
	
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
	 * For identical content digest revisit records, the file where the payload
	 * can be found, if known.
	 */
	public static final String CAPTURE_DUPLICATE_PAYLOAD_FILE = "payload-" + CAPTURE_FILE;

	/**
	 * For identical content digest revisit records, the offset in
	 * CAPTURE_DUPLICATE_PAYLOAD_FILE where the payload record can be found, if
	 * known.
	 */
	public static final String CAPTURE_DUPLICATE_PAYLOAD_OFFSET = "payload-" + CAPTURE_OFFSET;
	
	
	/**
	 * For identical content digest revisit records, the compressed length in
	 * CAPTURE_DUPLICATE_PAYLOAD_LENGTH where the payload record can be found, if
	 * known.
	 */
	public static final String CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH = "payload-" + CAPTURE_COMPRESSED_LENGTH;

	/**
	 * flag indicates that this document was NOT downloaded, but that the
	 * origin server indicated that the document had not changed, based on
	 * If-Modified HTTP request headers.
	 */
	public static final String CAPTURE_DUPLICATE_HTTP = "http";

	/**
	 * @return the original URL which resulted in the capture. If it is not 
	 * available, the urlKey and original Host will be used to reconstruct 
	 * something possibly closer to the original URL than the urlKey
	 */
	public String getOriginalUrl() {
		String url = get(CAPTURE_ORIGINAL_URL);
		if(url == null) {
			// convert from ORIG_HOST to ORIG_URL here:
			url = getUrlKey();
			String host = get(CAPTURE_ORIGINAL_HOST);
			if(url != null && host != null) {
				StringBuilder sb = new StringBuilder(url.length());
				sb.append(UrlOperations.DEFAULT_SCHEME);
				sb.append(host);
				sb.append(UrlOperations.getURLPath(url));
				url = sb.toString();
				// cache it for next time...?
				setOriginalUrl(url);
			}
		}
		return url;
	}
	/**
	 * @param originalUrl as close to the original URL by which this Resource
	 * 			was captured as is possible
	 */
	public void setOriginalUrl(String originalUrl) {
		put(CAPTURE_ORIGINAL_URL,originalUrl);
	}
	public String getOriginalHost() {
		String host = get(CAPTURE_ORIGINAL_HOST);
		if(host == null) {
			host = UrlOperations.urlToHost(getOriginalUrl());
		}
		return host;
	}
	public void setOriginalHost(String originalHost) {
		put(CAPTURE_ORIGINAL_HOST,originalHost);
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
	public long getCompressedLength() {
		if(cachedCompressedLength == -1) {
			String tmp = get(CAPTURE_COMPRESSED_LENGTH);
			cachedCompressedLength = tmp == null ? -1 : Long.parseLong(tmp);
		}
		return cachedCompressedLength;
	}
	public void setCompressedLength(long offset) {
		cachedCompressedLength = offset;
		put(CAPTURE_COMPRESSED_LENGTH,String.valueOf(offset));
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

	/*
	 * Identical content digest revisits have a duplicateDigestStoredDate if the
	 * payload is found by WARCRevisitAnnotationFilter in an earlier capture of
	 * the same url. If isDuplicateDigest() and
	 * getDuplicateDigestStoredTimestamp()==null then it must be a url-agnostic
	 * HER-2022 revisit.
	 */
	
	public void flagDuplicateDigest() {
		put(CAPTURE_DUPLICATE_ANNOTATION,CAPTURE_DUPLICATE_DIGEST);
	}
	
	public void flagDuplicateDigest(CaptureSearchResult payload) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS, payload.getCaptureTimestamp());
		put(CAPTURE_DUPLICATE_PAYLOAD_FILE, payload.getFile());
		put(CAPTURE_DUPLICATE_PAYLOAD_OFFSET, String.valueOf(payload.getOffset()));
		if (payload.getCompressedLength() > 0) {
			put(CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH, String.valueOf(payload.getCompressedLength()));
		}
	}
	
	public String getDuplicatePayloadFile() {
		return get(CAPTURE_DUPLICATE_PAYLOAD_FILE);
	}
	
	public Long getDuplicatePayloadOffset() {
		if (get(CAPTURE_DUPLICATE_PAYLOAD_OFFSET) != null) {
			return Long.valueOf(get(CAPTURE_DUPLICATE_PAYLOAD_OFFSET));
		} else {
			return null;
		}
	}
	
	public long getDuplicatePayloadCompressedLength() {
		if (get(CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH) != null) {
			return Long.valueOf(get(CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH));
		} else {
			return -1;
		}
	}
	
	/** @deprecated */
	public void flagDuplicateDigest(Date storedDate) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS,dateToTS(storedDate));
	}
	/** @deprecated */
	public void flagDuplicateDigest(String storedTS) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS,storedTS);
	}
	
	public boolean isDuplicateDigest() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		return (dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_DIGEST));
	}
	public Date getDuplicateDigestStoredDate() {
		if(isDuplicateDigest() && get(CAPTURE_DUPLICATE_STORED_TS) != null) {
			return tsToDate(get(CAPTURE_DUPLICATE_STORED_TS));
		}
		return null;
	}
	public String getDuplicateDigestStoredTimestamp() {
		if(isDuplicateDigest()) {
			return get(CAPTURE_DUPLICATE_STORED_TS);
		}
		return null;
	}

	public void flagDuplicateHTTP(Date storedDate) {
		put(CAPTURE_DUPLICATE_ANNOTATION,CAPTURE_DUPLICATE_HTTP);
		put(CAPTURE_DUPLICATE_STORED_TS,dateToTS(storedDate));
	}
	public void flagDuplicateHTTP(String storedTS) {
		put(CAPTURE_DUPLICATE_ANNOTATION,CAPTURE_DUPLICATE_HTTP);
		put(CAPTURE_DUPLICATE_STORED_TS,storedTS);
	}
	public boolean isDuplicateHTTP() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		return (dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_HTTP));
	}
	public Date getDuplicateHTTPStoredDate() {
		if(isDuplicateHTTP()) {
			return tsToDate(get(CAPTURE_DUPLICATE_STORED_TS));
		}
		return null;
	}
	public String getDuplicateHTTPStoredTimestamp() {
		if(isDuplicateHTTP()) {
			return get(CAPTURE_DUPLICATE_STORED_TS);
		}
		return null;
	}
	public String getRobotFlags() {
		return get(CAPTURE_ROBOT_FLAGS);
	}
	public void setRobotFlags(String robotFlags) {
		put(CAPTURE_ROBOT_FLAGS,robotFlags);
	}
	public void setRobotFlag(String flag) {
		String flags = get(CAPTURE_ROBOT_FLAGS);
		if(flags == null) {
			flags = "";
		}
		if(!flags.contains(flag)) {
			flags = flags + flag;
		}
		put(CAPTURE_ROBOT_FLAGS,flags);
	}
	public boolean isRobotFlagSet(String flag) {
		String flags = get(CAPTURE_ROBOT_FLAGS);
		if(flags == null) {
			return false;
		}
		return flags.contains(flag); 
	}

	public boolean isRobotNoArchive() {
		return isRobotFlagSet(CAPTURE_ROBOT_NOARCHIVE);
	}
	public boolean isRobotNoIndex() {
		return isRobotFlagSet(CAPTURE_ROBOT_NOINDEX);
	}
	public boolean isRobotNoFollow() {
		return isRobotFlagSet(CAPTURE_ROBOT_NOFOLLOW);
	}
	public void setRobotNoArchive() {
		setRobotFlag(CAPTURE_ROBOT_NOARCHIVE);
	}
	public void setRobotNoIndex() {
		setRobotFlag(CAPTURE_ROBOT_NOARCHIVE);
	}
	public void setRobotNoFollow() {
		setRobotFlag(CAPTURE_ROBOT_NOARCHIVE);
	}
	
	public void setPrevResult(CaptureSearchResult result)
	{
		this.prevResult = result;
	}
	
	public CaptureSearchResult getPrevResult()
	{
		return this.prevResult;
	}
	
	public void setNextResult(CaptureSearchResult result)
	{
		this.nextResult = result;
	}
	
	public CaptureSearchResult getNextResult()
	{
		return this.nextResult;
	}
	
	public void removeFromList()
	{
		if (nextResult != null) {
			nextResult.setPrevResult(prevResult);
		}
		
		if (prevResult != null) {
			prevResult.setNextResult(nextResult);
		}
		
		prevResult = null;
		nextResult = null;
	}
	
	public String toString()
	{
		return this.getCaptureDate().toString() + " " + this.getOriginalUrl();
	}
}
