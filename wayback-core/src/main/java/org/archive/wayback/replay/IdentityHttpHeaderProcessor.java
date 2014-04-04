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

import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;

/**
 * HttpHeaderProcessor which passes through all headers as-is.
 *
 * <p>{@code Transfer-Encoding} header is an exception. It is always dropped
 * (or preserved by renaming if {@code prefix} is non-empty.) This is because
 * Resource classes always produce original content, decoding transfer-encoding.</p>
 * <p>TODO: This may not be the best place to drop {@code Transfer-Encoding}
 * header. Maybe better done inside Resource classes, who <em>knows</em> it
 * is decoding transfer-encoding.</p>
 *
 * @author brad
 */
public class IdentityHttpHeaderProcessor extends PreservingHttpHeaderProcessor {

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HttpHeaderProcessor#filter(java.util.Map, java.lang.String, java.lang.String, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.CaptureSearchResult)
	 */
	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {
		if (key.equalsIgnoreCase(HTTP_TRANSFER_ENCODING_HEADER_UP))
			preserve(output, key, value);
		else
			output.put(key, value);
	}
}
