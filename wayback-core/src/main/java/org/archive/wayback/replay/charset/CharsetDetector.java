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

/**
 * Abstract class containing common methods for determining the character 
 * encoding of a text Resource, most of which should be refactored into a
 * Util package.
 * <p>Actual <em>character encoding sniffing</em> algorithm steps are
 * now implemented by {@link EncodingSniffer}.</p>
 * @author brad
 * @see EncodingSniffer
 */
public abstract class CharsetDetector {
	/** the default charset name to use when giving up */
	public final static String DEFAULT_CHARSET = "UTF-8";

	/**
	 * @param resource {@code Resource} whose charset encoding is to be detected.
	 * @param request WaybackRequest which may contain additional hints to
	 *        processing
	 * @return String charset name for the Resource
	 * @throws IOException if there are problems reading the Resource
	 */
	public String getCharset(Resource resource, WaybackRequest request)
			throws IOException {
		return getCharset(resource, resource, request);
	}

	/**
	 * @param httpHeadersResource resource with http headers to consider 
	 * @param payloadResource resource with payload to consider (presumably text)
	 * @param request WaybackRequest which may contain additional hints to
	 *        processing
	 * @return String charset name for the Resource
	 * @throws IOException if there are problems reading the Resource
	 */
	public abstract String getCharset(Resource httpHeadersResource,
			Resource payloadResource, WaybackRequest wbRequest)
					throws IOException;
}
