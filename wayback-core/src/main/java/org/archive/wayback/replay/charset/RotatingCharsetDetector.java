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
 * @author brad
 *
 * Provides a way to rotate through several detection schemes 
 */
public class RotatingCharsetDetector extends CharsetDetector {
	private static final EncodingSniffer[] SNIFFERS = {
		new ContentTypeHeaderSniffer(),
		new PrescanMetadataSniffer(),
		new UniversalChardetSniffer()
	};
	public final static int MODES[][] = {
		{0,1,2},
		{0,2,1},
		{1,0,2},
		{1,2,0},
		{2,1,0},
		{2,0,1}
	};
	public final static int MODE_COUNT = MODES.length;
	public final static int GUESS_TYPES = SNIFFERS.length;

	public int nextMode(int curMode) {
		if (++curMode >= MODES.length) {
			curMode = 0;
		}
		return curMode;
	}

	public String getCharset(Resource httpHeadersResource,
			Resource payloadResource, int mode) throws IOException {
		return getCharset(new CompositeResource(httpHeadersResource, payloadResource), mode);
	}

	public String getCharset(Resource resource, int mode) throws IOException {
		if (mode >= MODES.length) {
			mode = 0;
		}
		int[] indexes = MODES[mode];
		for (int index : indexes) {
			String charset = SNIFFERS[index].sniff(resource);
			if (charset != null)
				return charset;
		}
		return DEFAULT_CHARSET;
	}

	@Override
	public String getCharset(Resource httpHeadersResource,
			Resource payloadResource, WaybackRequest request)
			throws IOException {
		int mode = request.getCharsetMode();
		return getCharset(httpHeadersResource, payloadResource, mode);
	}
}
