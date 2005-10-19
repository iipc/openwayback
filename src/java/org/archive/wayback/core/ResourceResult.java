/* ResourceResult
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.text.ParseException;

import org.archive.io.arc.ARCLocation;

/**
 * Encapsulates the data for a single Resource (in an ARC file) returned from a
 * ResourceIndex query.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ResourceResult {
	private final static String CDX_HEADER_STRING = " CDX N b h m s k r V g";

	private String url = null;

	private Timestamp timestamp = null;

	private String origHost = null;

	private String mimeType = null;

	private String httpResponseCode = null;

	private String md5Fragment = null;

	private String redirectUrl = null;

	private long compressedOffset = -1;

	private String arcFileName = null;

	/**
	 * Constructor
	 */
	public ResourceResult() {
		super();
	}

	/**
	 * get the ARCLocation object corresponding to this ResourceResult.
	 * 
	 * @return ARCLocation object.
	 */
	public ARCLocation getARCLocation() {
		final String daArcName = arcFileName;
		final long daOffset = compressedOffset;
		return new ARCLocation() {
			private String filename = daArcName;

			private long offset = daOffset;

			public String getName() {
				return this.filename;
			}

			public long getOffset() {
				return this.offset;
			}
		};
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
		timestamp = Timestamp.parseBefore(tokens[1]);
		origHost = tokens[2];
		mimeType = tokens[3];
		httpResponseCode = tokens[4];
		md5Fragment = tokens[5];
		redirectUrl = tokens[6];
		compressedOffset = Long.parseLong(tokens[7]);
		arcFileName = tokens[8];
	}

	/**
	 * get the CDX header line for the format serialized and deserialized in
	 * flat file format.
	 * 
	 * @return String representation of the CDX header line, WITHOUT NEWLINE.
	 */
	public static String getCDXHeaderString() {
		return CDX_HEADER_STRING;
	}

	public String toString() {
		return url + " " + timestamp.getDateStr() + " " + origHost + " "
				+ mimeType + " " + httpResponseCode + " " + md5Fragment + " "
				+ redirectUrl + " " + compressedOffset + " " + arcFileName;
	}

	/**
	 * @return arcFileName property
	 */
	public String getArcFileName() {
		return arcFileName;
	}

	/**
	 * @return compressedOffset property
	 */
	public long getCompressedOffset() {
		return compressedOffset;
	}

	/**
	 * @return String representation of the HTTP response code property.
	 */
	public String getHttpResponseCode() {
		return httpResponseCode;
	}

	/**
	 * @return MD5 digest property in hex-dec format, possible truncated to less
	 *         than 32 characters.
	 */
	public String getMd5Fragment() {
		return md5Fragment;
	}

	/**
	 * @return mimeType property
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return the original fully qualified String hostname from which this
	 *         resource was acquired.
	 */
	public String getOrigHost() {
		return origHost;
	}

	/**
	 * @return the String URL to which this resource redirects, or "-" if it
	 *         does not redirect.
	 */
	public String getRedirectUrl() {
		return redirectUrl;
	}

	/**
	 * @return true if this resource is though to redirect to another URL, false
	 *         otherwise.
	 */
	public boolean isRedirect() {
		return (0 != redirectUrl.compareTo("-"));
	}

	/**
	 * @return Returns the timestamp.
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            The timestamp to set.
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param arcFileName
	 *            The arcFileName to set.
	 */
	public void setArcFileName(String arcFileName) {
		this.arcFileName = arcFileName;
	}

	/**
	 * @param compressedOffset
	 *            The compressedOffset to set.
	 */
	public void setCompressedOffset(long compressedOffset) {
		this.compressedOffset = compressedOffset;
	}

	/**
	 * @param httpResponseCode
	 *            The httpResponseCode to set.
	 */
	public void setHttpResponseCode(String httpResponseCode) {
		this.httpResponseCode = httpResponseCode;
	}

	/**
	 * @param md5Fragment
	 *            The md5Fragment to set.
	 */
	public void setMd5Fragment(String md5Fragment) {
		this.md5Fragment = md5Fragment;
	}

	/**
	 * @param mimeType
	 *            The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @param origHost
	 *            The origHost to set.
	 */
	public void setOrigHost(String origHost) {
		this.origHost = origHost;
	}

	/**
	 * @param redirectUrl
	 *            The redirectUrl to set.
	 */
	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
