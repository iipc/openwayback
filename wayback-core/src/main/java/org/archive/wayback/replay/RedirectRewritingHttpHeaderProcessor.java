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
package org.archive.wayback.replay;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RedirectRewritingHttpHeaderProcessor 
	implements HttpHeaderProcessor {

	private static String DEFAULT_PREFIX = null;
	private String prefix = DEFAULT_PREFIX; 
	private Set<String> passThroughHeaders = null;
	private Set<String> rewriteHeaders = null;
	
	public RedirectRewritingHttpHeaderProcessor() {
		passThroughHeaders = new HashSet<String>();
		passThroughHeaders.add(HTTP_CONTENT_TYPE_HEADER_UP);
		passThroughHeaders.add(HTTP_CONTENT_DISP_HEADER_UP);
		
		rewriteHeaders = new HashSet<String>();
		rewriteHeaders.add(HTTP_LOCATION_HEADER_UP);
		rewriteHeaders.add(HTTP_CONTENT_LOCATION_HEADER_UP);
		rewriteHeaders.add(HTTP_CONTENT_BASE_HEADER_UP);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HttpHeaderProcessor#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.CaptureSearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {

		String keyUp = key.toUpperCase();

		// first stick it in as-is, or with prefix, then maybe we'll overwrite
		// with the later logic.
		if(prefix == null) {
			if(!keyUp.equals(HTTP_LENGTH_HEADER_UP)) {
				output.put(key, value);
			}
		} else {
			output.put(prefix + key, value);
		}

		// rewrite Location header URLs
		if(rewriteHeaders.contains(keyUp)) {
//		if (keyUp.startsWith(HTTP_LOCATION_HEADER_UP) ||
//			keyUp.startsWith(HTTP_CONTENT_LOCATION_HEADER_UP) ||
//			keyUp.startsWith(HTTP_CONTENT_BASE_HEADER_UP)) {

			String baseUrl = result.getOriginalUrl();
			String cd = result.getCaptureTimestamp();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);

			output.put(key, uriConverter.makeReplayURI(cd,u));

//		} else if(keyUp.startsWith(HTTP_CONTENT_TYPE_HEADER_UP)) {
		} else if(passThroughHeaders.contains(keyUp)) {
			// let's leave this one as-is:
			output.put(key,value);
		}
	}
}
