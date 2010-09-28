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
package org.archive.wayback.resourceindex.cdx;


import java.util.logging.Logger;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.UrlOperations;

/**
 * Adapter that converts a CDX record String into a CaptureSearchResult
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXLineToSearchResultAdapter implements Adapter<String,CaptureSearchResult> {

	private static final Logger LOGGER = Logger.getLogger(
			CDXLineToSearchResultAdapter.class.getName());
	
	private final static String SCHEME_STRING = "://";
	private final static String DEFAULT_SCHEME = "http://";
	
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

	public CaptureSearchResult adapt(String line) {
		return doAdapt(line);
	}
	/**
	 * @param line
	 * @return SearchResult representation of input line
	 */
	public static CaptureSearchResult doAdapt(String line) {
		CaptureSearchResult result = new CaptureSearchResult();
		String[] tokens = line.split(" ");
		boolean hasRobotFlags = false;
		if (tokens.length != 9) {
			if(tokens.length == 10) {
				hasRobotFlags = true;
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

		if(!tokens[nextToken].equals("-")) {
			try {
				compressedOffset = Long.parseLong(tokens[nextToken]);
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
		result.setFile(fileName);

		return result;
	}
}
