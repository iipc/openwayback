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
 * {@link HttpHeaderProcessor} that preserves all headers by prepending a prefix,
 * translates URL in resource location headers and pass-through certain headers.
 * <p>Headers rewritten:
 * <ul>
 * <li>{@code Location}</li>
 * <li>{@code Content-Location}</li>
 * <li>{@code Content-Base}</li>
 * </ul>
 * Headers passed-through:
 * <ul>
 * <li>{@code Content-Type}</li>
 * <li>{@code Content-Disposition}</li>
 * </ul>
 * </p>
 * <p>If {@code prefix} property is {@code null} (default), all headers but {@code Content-Length}
 * are copied as they are. With non-{@code null} prefix, all headers, including
 * {@code Length} are preserved by prepending header name with {@code prefix}.</p>
 * <p>Caveat: if {@code prefix} is an empty string, all headers including {@code Content-Length}
 * are copied as they are. This is presumably a bug.</p>
 * <p>Intended for archival-URL and domain-prefix mode.</p>
 *
 * @author brad
 */
public class RedirectRewritingHttpHeaderProcessor extends PreservingHttpHeaderProcessor {

	private Set<String> passThroughHeaders = null;
	private Set<String> rewriteHeaders = null;
	private Set<String> dropHeaders;
	
	public RedirectRewritingHttpHeaderProcessor() {
		passThroughHeaders = new HashSet<String>();
		passThroughHeaders.add(HTTP_CONTENT_TYPE_HEADER_UP);
		passThroughHeaders.add(HTTP_CONTENT_DISP_HEADER_UP);
		
		rewriteHeaders = new HashSet<String>();
		rewriteHeaders.add(HTTP_LOCATION_HEADER_UP);
		rewriteHeaders.add(HTTP_CONTENT_LOCATION_HEADER_UP);
		rewriteHeaders.add(HTTP_CONTENT_BASE_HEADER_UP);

		dropHeaders = new HashSet<String>();
		dropHeaders.add(HTTP_LENGTH_HEADER_UP);
		dropHeaders.add(HTTP_TRANSFER_ENCODING_HEADER_UP);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HttpHeaderProcessor#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.CaptureSearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {

		String keyUp = key.toUpperCase();

		// first stick it in as-is, or with prefix, then maybe we'll overwrite
		// with the later logic.
		if (dropHeaders.contains(keyUp))
			preserve(output, key, value);
		else
			preserveAlways(output, key, value);

		// rewrite Location header URLs
		if(rewriteHeaders.contains(keyUp)) {
			String baseUrl = result.getOriginalUrl();
			String cd = result.getCaptureTimestamp();
			// by the spec, these should be absolute already, but just in case:
			String u = UrlOperations.resolveUrl(baseUrl, value);
			output.put(key, uriConverter.makeReplayURI(cd,u));
		} else if(passThroughHeaders.contains(keyUp)) {
			// let's leave this one as-is:
			output.put(key,value);
		}
	}
}
