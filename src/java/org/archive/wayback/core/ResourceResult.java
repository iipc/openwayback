package org.archive.wayback.core;

import java.text.ParseException;

import org.archive.io.arc.ARCLocation;

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

	public ResourceResult() {
		super();
	}

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

	public static String getCDXHeaderString() {
		return CDX_HEADER_STRING;
	}

	public String toString() {
		return url + " " + timestamp.getDateStr() + " " + origHost + " "
				+ mimeType + " " + httpResponseCode + " " + md5Fragment + " "
				+ redirectUrl + " " + compressedOffset + " " + arcFileName;
	}

	public String toShortString() {
		return url + "\t" + timestamp.getDateStr() + "\t" + compressedOffset
				+ "\t" + arcFileName;
	}

	public String getArcFileName() {
		return arcFileName;
	}

	public long getCompressedOffset() {
		return compressedOffset;
	}

	public String getHttpResponseCode() {
		return httpResponseCode;
	}

	public String getMd5Fragment() {
		return md5Fragment;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getOrigHost() {
		return origHost;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public boolean isRedirect() {
		return (0 != redirectUrl.compareTo("-"));
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public String getUrl() {
		return url;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void setArcFileName(String arcFileName) {
		this.arcFileName = arcFileName;
	}

	public void setCompressedOffset(long compressedOffset) {
		this.compressedOffset = compressedOffset;
	}

	public void setHttpResponseCode(String httpResponseCode) {
		this.httpResponseCode = httpResponseCode;
	}

	public void setMd5Fragment(String md5Fragment) {
		this.md5Fragment = md5Fragment;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setOrigHost(String origHost) {
		this.origHost = origHost;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timestamp = timeStamp;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
