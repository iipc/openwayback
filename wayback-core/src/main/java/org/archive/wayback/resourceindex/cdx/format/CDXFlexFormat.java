package org.archive.wayback.resourceindex.cdx.format;

import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.url.UrlOperations;

public class CDXFlexFormat extends CDXFormat {
	private final static String SCHEME_STRING = "://";
	private final static String DEFAULT_SCHEME = "http://";

	private static final Logger LOGGER = 
		Logger.getLogger(CDXFlexFormat.class.getName());
	public CDXFlexFormat(String cdxSpec) throws CDXFormatException {
		super(cdxSpec);
	}
	private static int getEndOfHostIndex(String url) {
		int portIdx = url.indexOf(UrlOperations.PORT_SEPARATOR);
		int pathIdx = url.indexOf(UrlOperations.PATH_START);
		if(portIdx == -1 && pathIdx == -1) {
			return url.length();
		}
		if(portIdx == -1) {
			return pathIdx;
		}
		if(pathIdx == -1) {
			return portIdx;
		}
		if(pathIdx > portIdx) {
			return portIdx;
		} else {
			return pathIdx;
		}
	}
	
	// Single place to do the flex cdx-line parsing logic
	public static CaptureSearchResult parseCDXLineFlex(String line) {
		CaptureSearchResult result = new CaptureSearchResult();
		String[] tokens = line.split(" ");
		boolean hasRobotFlags = false;
		boolean hasLengthFlag = false;
		if (tokens.length != 9) {
			hasRobotFlags = true;
			if(tokens.length == 10) {
			} else if(tokens.length == 11) {
				hasLengthFlag = true;
			} else {
				return null;
			}
			//throw new IllegalArgumentException("Need 9 columns("+line+")");
		}
		String urlKey = tokens[0];
		String captureTS = tokens[1];
		String originalUrl = tokens[2];
		
		// convert from ORIG_HOST to ORIG_URL here:
		if(!originalUrl.contains(SCHEME_STRING)) {
			StringBuilder sb = new StringBuilder(urlKey.length());
			sb.append(DEFAULT_SCHEME);
			sb.append(originalUrl);
			sb.append(urlKey.substring(getEndOfHostIndex(urlKey)));
			originalUrl = sb.toString();
		}
		String mimeType = tokens[3];
		String httpCode = tokens[4];
		String digest = tokens[5];
		String redirectUrl = tokens[6];
		long compressedOffset = -1;
		int nextToken = 7;
		if(hasRobotFlags) {
			result.setRobotFlags(tokens[nextToken]);
			nextToken++;
		}
		String length = "-";
		if(hasLengthFlag) {
			length = tokens[nextToken];
			nextToken++;
		}

		if(!tokens[nextToken].equals("-")) {
			try {
				compressedOffset = Long.parseLong(tokens[nextToken]);
				if(!length.equals("-")) {
					// try to set the endOffset:
					result.setCompressedLength(Long.parseLong(length));
				}
			} catch (NumberFormatException e) {
				LOGGER.warning("Bad compressed Offset field("+nextToken+") in (" +
						line +")");
				return null;
			}
		}
		nextToken++;
		String fileName = tokens[nextToken];
		result.setUrlKey(urlKey);
		result.setCaptureTimestamp(captureTS);
		result.setOriginalUrl(originalUrl);
		result.setMimeType(mimeType);
		result.setHttpCode(httpCode);
		result.setDigest(digest);
		result.setRedirectUrl(redirectUrl);
		result.setOffset(compressedOffset);
		result.setFile(fileName.trim());

		return result;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.cdx.format.CDXFormat#parseResult(java.lang.String)
	 */
	@Override
	public CaptureSearchResult parseResult(String line)
			throws CDXFormatException {
		return CDXFlexFormat.parseCDXLineFlex(line);
	}

}
