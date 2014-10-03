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
package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.CompositeResource;

/**
 * {@link CharsetDetector} that roughly follows steps prescribed by
 * <a href="http://www.whatwg.org/specs/web-apps/current-work/multipage/parsing.html#encoding-sniffing-algorithm">WHAT-WG recommendation:</a>,
 * with following simplifications:
 * <ul>
 * <li>no support for inheriting parent browsing context's character encoding
 * (information is not readily available to Wayback)</li>
 * <li>default is fixed to {@code UTF-8}, regardless of user's locale (a crawler's
 * locale information is not readily available to Wayback)</li>
 * <li>does not support <em>confidence</em>, thus does not support
 * <em>encoding switching</em> (this is more about {@code CharsetDetector}'s
 * design)</ul>
 * </ul>
 * <p>CHANGE 1.8.1 2014-07-07: added BOM detection as the first step.</p>
 */
public class StandardCharsetDetector extends CharsetDetector {
	private final static EncodingSniffer[] SNIFFERS = {
		new ByteOrderMarkSniffer(),
		new ContentTypeHeaderSniffer(),
		new PrescanMetadataSniffer(),
		new UniversalChardetSniffer()
	};

	@Override
	public String getCharset(Resource httpHeadersResource,
			Resource payloadResource, WaybackRequest wbRequest) throws IOException {
		Resource resource = httpHeadersResource != payloadResource ? new CompositeResource(
			httpHeadersResource, payloadResource) : payloadResource;
		for (EncodingSniffer sniffer : SNIFFERS) {
			String charset = sniffer.sniff(resource);
			if (charset != null)
				return charset;
		}
		return DEFAULT_CHARSET;
	}
}
