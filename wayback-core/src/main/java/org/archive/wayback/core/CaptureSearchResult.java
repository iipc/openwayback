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

import org.archive.wayback.ResourceIndex;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 */
public class CaptureSearchResult extends SearchResult implements Capture {

	protected long cachedOffset = -1;
	protected long cachedCompressedLength = -1;
	protected long cachedDate = -1;

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
	 * Result: 3-digit integer HTTP response code. may be '0' in some fringe
	 * conditions, old ARCs, bug in crawler, etc.
	 */
	public static final String CAPTURE_HTTP_CODE = "httpresponsecode";

	/**
	 * Result: some form of document fingerprint. This should represent the HTTP
	 * payload only for HTTP captured resources. It may represent an MD5, a
	 * SHA1, and may be a fragment of the full representation of the digest.
	 */
	public static final String CAPTURE_DIGEST = "digest";

	/**
	 * Result: URL that this document redirected to, or '-' if it does not
	 * redirect
	 */
	public static final String CAPTURE_REDIRECT_URL = "redirecturl";

	/**
	 * Result: String flags which indicate robot instructions found in an HTML
	 * page. Currently one or more of:
	 * <ul>
	 * <li>"A" - noarchive</li>
	 * <li>"F" - nofollow</li>
	 * <li>"I" - noindex</li>
	 * </ul>
	 * @see "http://noarchive.net/"
	 */
	public static final String CAPTURE_ROBOT_FLAGS = "robotflags";

	public static final String CAPTURE_ROBOT_NOARCHIVE = "A";
	public static final String CAPTURE_ROBOT_NOFOLLOW = "F";
	public static final String CAPTURE_ROBOT_NOINDEX = "I";

	public static final String CAPTURE_ROBOT_IGNORE = "G";

	/**
	 * non-standard robot-flag indicating the capture is <i>soft-blocked</i>
	 * (not available for direct replay, but available as the original for
	 * a revisits.)
	 */
	public static final char CAPTURE_ROBOT_BLOCKED = 'X';

	/**
	 * Result: flag within a SearchResult that indicates this is the closest to
	 * a particular requested date.
	 */
	public static final String CAPTURE_CLOSEST_INDICATOR = "closest";
	public static final String CAPTURE_CLOSEST_VALUE = "true";

	/**
	 * Result: this key being present indicates that this particular capture was
	 * not actually stored, and that other values within this SearchResult are
	 * actually values from a different record which *should* be identical to
	 * this capture, had it been stored.
	 */
	public static final String CAPTURE_DUPLICATE_ANNOTATION = "duplicate";

	/**
	 * Result: this key is present when the CAPTURE_DUPLICATE_ANNOTATION is also
	 * present, with the value indicating the last date that was actually stored
	 * for this duplicate.
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
	public static final String CAPTURE_DUPLICATE_PAYLOAD_FILE = "payload-" +
			CAPTURE_FILE;

	/**
	 * For identical content digest revisit records, the offset in
	 * CAPTURE_DUPLICATE_PAYLOAD_FILE where the payload record can be found, if
	 * known.
	 */
	public static final String CAPTURE_DUPLICATE_PAYLOAD_OFFSET = "payload-" +
			CAPTURE_OFFSET;

	/**
	 * For identical content digest revisit records, the compressed length in
	 * CAPTURE_DUPLICATE_PAYLOAD_LENGTH where the payload record can be found,
	 * if known.
	 */
	public static final String CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH = "payload-" +
			CAPTURE_COMPRESSED_LENGTH;

	/**
	 * flag indicates that this document was NOT downloaded, but that the origin
	 * server indicated that the document had not changed, based on If-Modified
	 * HTTP request headers.
	 */
	public static final String CAPTURE_DUPLICATE_HTTP = "http";

	public static final String CAPTURE_ORACLE_POLICY = "oracle-policy";

	public CaptureSearchResult() {

	}

	protected CaptureSearchResult(boolean autocreateMap) {
		super(autocreateMap);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.core.Capture#getOriginalUrl()
	 */
	@Override
	public String getOriginalUrl() {
		String url = get(CAPTURE_ORIGINAL_URL);
		if (url == null) {
			// convert from ORIG_HOST to ORIG_URL here:
			url = getUrlKey();
			String host = get(CAPTURE_ORIGINAL_HOST);
			if (url != null && host != null) {
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
	 *        was captured as is possible
	 */
	public void setOriginalUrl(String originalUrl) {
		put(CAPTURE_ORIGINAL_URL, originalUrl);
	}

	public String getOriginalHost() {
		String host = get(CAPTURE_ORIGINAL_HOST);
		if (host == null) {
			host = UrlOperations.urlToHost(getOriginalUrl());
		}
		return host;
	}

	public void setOriginalHost(String originalHost) {
		put(CAPTURE_ORIGINAL_HOST, originalHost);
	}

	public String getUrlKey() {
		return get(CAPTURE_URL_KEY);
	}

	public void setUrlKey(String urlKey) {
		put(CAPTURE_URL_KEY, urlKey);
	}

	public Date getCaptureDate() {
		if (cachedDate == -1) {
			cachedDate = tsToDate(getCaptureTimestamp()).getTime();
		}
		return new Date(cachedDate);
	}

	public void setCaptureDate(Date date) {
		cachedDate = date.getTime();
		setCaptureTimestamp(dateToTS(date));
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.core.Capture#getCaptureTimestamp()
	 */
	@Override
	public String getCaptureTimestamp() {
		return get(CAPTURE_CAPTURE_TIMESTAMP);
	}

	public void setCaptureTimestamp(String timestamp) {
		put(CAPTURE_CAPTURE_TIMESTAMP, timestamp);
	}

	public String getFile() {
		return get(CAPTURE_FILE);
	}

	public void setFile(String file) {
		put(CAPTURE_FILE, file);
	}

	public long getOffset() {
		if (cachedOffset == -1) {
			cachedOffset = Long.parseLong(get(CAPTURE_OFFSET));
		}
		return cachedOffset;
	}

	public void setOffset(long offset) {
		cachedOffset = offset;
		put(CAPTURE_OFFSET, String.valueOf(offset));
	}

	public long getCompressedLength() {
		if (cachedCompressedLength == -1) {
			String tmp = get(CAPTURE_COMPRESSED_LENGTH);
			cachedCompressedLength = tmp == null ? -1 : Long.parseLong(tmp);
		}
		return cachedCompressedLength;
	}

	public void setCompressedLength(long offset) {
		cachedCompressedLength = offset;
		put(CAPTURE_COMPRESSED_LENGTH, String.valueOf(offset));
	}

	public String getMimeType() {
		return get(CAPTURE_MIME_TYPE);
	}

	public void setMimeType(String mimeType) {
		put(CAPTURE_MIME_TYPE, mimeType);
	}

	public String getHttpCode() {
		return get(CAPTURE_HTTP_CODE);
	}

	public void setHttpCode(String httpCode) {
		put(CAPTURE_HTTP_CODE, httpCode);
	}

	public String getDigest() {
		return get(CAPTURE_DIGEST);
	}

	public void setDigest(String digest) {
		put(CAPTURE_DIGEST, digest);
	}

	public String getRedirectUrl() {
		return get(CAPTURE_REDIRECT_URL);
	}

	public void setRedirectUrl(String url) {
		put(CAPTURE_REDIRECT_URL, url);
	}

	public boolean isClosest() {
		return getBoolean(CAPTURE_CLOSEST_INDICATOR);
	}

	public void setClosest(boolean value) {
		putBoolean(CAPTURE_CLOSEST_INDICATOR, value);
	}

	/*
	 * Identical content digest revisits have a duplicateDigestStoredDate if the
	 * payload is found by WARCRevisitAnnotationFilter in an earlier capture of
	 * the same url. If isDuplicateDigest() and
	 * getDuplicateDigestStoredTimestamp()==null then it must be a url-agnostic
	 * HER-2022 revisit.
	 */

	public void flagDuplicateDigest() {
		put(CAPTURE_DUPLICATE_ANNOTATION, CAPTURE_DUPLICATE_DIGEST);
	}

	/**
	 * Mark this capture as a revisit of previous capture {@code payload}, identified by content digest.
	 * <p>Record location information is copied from {@code payload} so that the content can be
	 * loaded from the record later.</p>
	 * <p>{@link ResourceIndex} implementations should call this method before returning
	 * {@code CaptureSearchResult}s to {@code AccessPoint}.</p>
	 * @param payload capture being revisited
	 * @see #getDuplicateDigestStoredTimestamp()
	 * @see #getDuplicateDigestStoredDate()
	 * @see #getDuplicatePayloadFile()
	 * @see #getDuplicatePayloadOffset()
	 * @see #getDuplicatePayloadCompressedLength()
	 */
	public void flagDuplicateDigest(CaptureSearchResult payload) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS, payload.getCaptureTimestamp());
		put(CAPTURE_DUPLICATE_PAYLOAD_FILE, payload.getFile());
		put(CAPTURE_DUPLICATE_PAYLOAD_OFFSET,
			String.valueOf(payload.getOffset()));
		if (payload.getCompressedLength() > 0) {
			put(CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH,
				String.valueOf(payload.getCompressedLength()));
		}
	}

	// For use in FastCaptureSearchResult, which stores the payload
	// CaptureSearchResult directly
	public CaptureSearchResult getDuplicatePayload() {
		return null;
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
			return Long
					.valueOf(get(CAPTURE_DUPLICATE_PAYLOAD_COMPRESSED_LENGTH));
		} else {
			return -1;
		}
	}

	/** @deprecated */
	public void flagDuplicateDigest(Date storedDate) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS, dateToTS(storedDate));
	}

	/** @deprecated */
	public void flagDuplicateDigest(String storedTS) {
		flagDuplicateDigest();
		put(CAPTURE_DUPLICATE_STORED_TS, storedTS);
	}

	/**
	 * whether this capture is a re-fetch of previously archived capture
	 * (<i>revisit</i>), detected by content's digest, and replay of
	 * that previous capture is not blocked.
	 * <p>1.8.1 2014-10-02 behavior change. This method now returns
	 * {@code false} even for revisits, if the original capture
	 * is blocked. Use #isRevisitDigest() for old behavior.</p>
	 * @return {@code true} if revisit
	 */
	public boolean isDuplicateDigest() {
		if (!isRevisitDigest()) return false;
		CaptureSearchResult orig = getDuplicatePayload();
		if (orig != null && orig.isRobotFlagSet(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED))
			return false;
		return true;
	}

	/**
	 * whether this capture is a re-fetch of previously archived capture
	 * (<i>revisit</i>), detected by content's digest.
	 * <p>This method is meant for use by replay processing. For use in
	 * user interface / web API code, consider {@link #isDuplicateDigest()}
	 * is more appropriate.</p>
	 * @return {@code true} if revisit
	 */
	public boolean isRevisitDigest() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		return (dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_DIGEST));
	}

	public Date getDuplicateDigestStoredDate() {
		if (isRevisitDigest() && get(CAPTURE_DUPLICATE_STORED_TS) != null) {
			return tsToDate(get(CAPTURE_DUPLICATE_STORED_TS));
		}
		return null;
	}

	/**
	 * same with {@link #getDuplicateDigestStoredDate()}, but
	 * returns raw timestamp value.
	 * @return string representing timestamp.
	 */
	public String getDuplicateDigestStoredTimestamp() {
		if (isRevisitDigest()) {
			return get(CAPTURE_DUPLICATE_STORED_TS);
		}
		return null;
	}

	public void flagDuplicateHTTP(Date storedDate) {
		put(CAPTURE_DUPLICATE_ANNOTATION, CAPTURE_DUPLICATE_HTTP);
		put(CAPTURE_DUPLICATE_STORED_TS, dateToTS(storedDate));
	}

	public void flagDuplicateHTTP(String storedTS) {
		put(CAPTURE_DUPLICATE_ANNOTATION, CAPTURE_DUPLICATE_HTTP);
		put(CAPTURE_DUPLICATE_STORED_TS, storedTS);
	}

	/**
	 * whether this capture is an archive of {@code 304 Not Modified} response
	 * from the server.
	 * @return
	 */
	public boolean isDuplicateHTTP() {
		String dupeType = get(CAPTURE_DUPLICATE_ANNOTATION);
		return (dupeType != null && dupeType.equals(CAPTURE_DUPLICATE_HTTP));
	}

	public Date getDuplicateHTTPStoredDate() {
		if (isDuplicateHTTP()) {
			return tsToDate(get(CAPTURE_DUPLICATE_STORED_TS));
		}
		return null;
	}

	public String getDuplicateHTTPStoredTimestamp() {
		if (isDuplicateHTTP()) {
			return get(CAPTURE_DUPLICATE_STORED_TS);
		}
		return null;
	}

	/**
	 * return <i>robot flags</i> field value.
	 * @return
	 */
	public String getRobotFlags() {
		return get(CAPTURE_ROBOT_FLAGS);
	}

	/**
	 * Set <i>robot flags</i> field value as a whole.
	 * For adding a flag, use {@link #setRobotFlag(char)} or
	 * {@link #setRobotFlag(String)}.
	 * @param robotFlags new field value
	 */
	public void setRobotFlags(String robotFlags) {
		put(CAPTURE_ROBOT_FLAGS, robotFlags);
	}
	/**
	 * Add a flag to {@code robotflags} field.
	 * If {@code flag} is already set, this is a no-op.
	 * @param flag a flag to add (don't put multiple flags).
	 */
	public void setRobotFlag(String flag) {
		String flags = getRobotFlags();
		if (flags == null) {
			flags = "";
		}
		if (!flags.contains(flag)) {
			flags = flags + flag;
		}
		setRobotFlags(flags);
	}

	/**
	 * Add a flag to {@code robotflags} field.
	 * If {@code flag} is already set, this is a no-op.
	 * @param flag a flag to add
	 */
	public void setRobotFlag(char flag) {
		String flags = getRobotFlags();
		if (flags == null) {
			setRobotFlags(Character.toString(flag));
		} else {
			if (flags.indexOf(flag) < 0) {
				setRobotFlags(flags + flag);
			}
		}
	}

	/**
	 * test if {@code robotflags} field has flag {@code flag} set.
	 * <p>
	 * Caveat: if {@code flag} has more than once character,
	 * {@code robotflags} must have {@code flag} as its substring
	 * for this method to return {@code true} (not really useful).
	 * </p>
	 * @param flag flag to test
	 * @return {@code true} if {@code flag} is set.
	 */
	public boolean isRobotFlagSet(String flag) {
		String flags = getRobotFlags();
		if (flags == null) {
			return false;
		}
		return flags.contains(flag);
	}

	/**
	 * test if {@code robotflags} field has flag {@code flag} set.
	 * @param flag one flag to test
	 * @return {@code true} if {@code flag} is set.
	 */
	public boolean isRobotFlagSet(char flag) {
		String flags = getRobotFlags();
		return flags != null && flags.indexOf(flag) >= 0;
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

	public boolean isRobotIgnore() {
		return isRobotFlagSet(CAPTURE_ROBOT_IGNORE);
	}

	public void setRobotNoArchive() {
		setRobotFlag(CAPTURE_ROBOT_NOARCHIVE);
	}

	public void setRobotNoIndex() {
		setRobotFlag(CAPTURE_ROBOT_NOINDEX);
	}

	public void setRobotNoFollow() {
		setRobotFlag(CAPTURE_ROBOT_NOFOLLOW);
	}

	public void setRobotIgnore() {
		setRobotFlag(CAPTURE_ROBOT_IGNORE);
	}

	public String getOraclePolicy() {
		return get(CAPTURE_ORACLE_POLICY);
	}

	public void setOraclePolicy(String policy) {
		put(CAPTURE_ORACLE_POLICY, policy);
	}

	public void setPrevResult(CaptureSearchResult result) {
		prevResult = result;
	}

	public CaptureSearchResult getPrevResult() {
		return prevResult;
	}

	public void setNextResult(CaptureSearchResult result) {
		nextResult = result;
	}

	public CaptureSearchResult getNextResult() {
		return nextResult;
	}

	public void removeFromList() {
		if (nextResult != null) {
			nextResult.setPrevResult(prevResult);
		}

		if (prevResult != null) {
			prevResult.setNextResult(nextResult);
		}

		prevResult = null;
		nextResult = null;
	}

	public String toString() {
		return getCaptureDate().toString() + " " + getOriginalUrl();
	}

	/**
	 * {@code true} if HTTP response code is either {@code 4xx} or {@code 5xx}.
	 * @return
	 */
	public boolean isHttpError() {
		if (isRevisitDigest() && (getDuplicatePayload() != null)) {
			return getDuplicatePayload().isHttpError();
		}
		String httpCode = getHttpCode();
		return (httpCode.startsWith("4") || httpCode.startsWith("5"));
	}

	/**
	 * {@code true} if HTTP response code is {@code 3xx}.
	 * @return
	 */
	public boolean isHttpRedirect() {
		if (isRevisitDigest() && (getDuplicatePayload() != null)) {
			return getDuplicatePayload().isHttpRedirect();
		}
		String httpCode = getHttpCode();
		return (httpCode.startsWith("3"));
	}

	/**
	 * {@code true} if HTTP response code is {@code 2xx}.
	 * @return
	 */
	public boolean isHttpSuccess() {
		if (isRevisitDigest() && (getDuplicatePayload() != null)) {
			return getDuplicatePayload().isHttpSuccess();
		}
		String httpCode = getHttpCode();
		return (httpCode.startsWith("2"));
	}
}
