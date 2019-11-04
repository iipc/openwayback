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

import java.util.Arrays;
import java.util.HashSet;

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

	public static final String[] DEFAULT_PASSTHROUGH_HEADERS = {
		HTTP_CONTENT_TYPE_HEADER_UP, HTTP_CONTENT_ENCODING_HEADER_UP,
		HTTP_CONTENT_DISP_HEADER_UP, HTTP_CONTENT_RANGE_HEADER_UP
	};
	public static final String[] DEFAULT_REWRITE_HEADERS = {
		HTTP_LOCATION_HEADER_UP, HTTP_CONTENT_LOCATION_HEADER_UP,
		HTTP_CONTENT_BASE_HEADER_UP
	};
	public static final String[] DEFAULT_DROP_HEADERS = {
		HTTP_LENGTH_HEADER_UP,
		HTTP_TRANSFER_ENCODING_HEADER_UP
	};
	public RedirectRewritingHttpHeaderProcessor() {
		passThroughHeaders = new HashSet<String>(Arrays.asList(DEFAULT_PASSTHROUGH_HEADERS));
		rewriteHeaders = new HashSet<String>(Arrays.asList(DEFAULT_REWRITE_HEADERS));
		dropHeaders = new HashSet<String>(Arrays.asList(DEFAULT_DROP_HEADERS));
	}

}
