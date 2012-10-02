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
 * @author brad
 *
 * Provides a way to rotate through several detection schemes 
 */
public class RotatingCharsetDetector extends CharsetDetector {
	public final static int MODES[][] = {
		{0,1,2},
		{0,2,1},
		{1,0,2},
		{1,2,0},
		{2,1,0},
		{2,0,1}
	};
	public final static int MODE_COUNT = 6;
	public final static int GUESS_TYPES = 3;

	public int nextMode(int curMode) {
		if(curMode >= MODE_COUNT - 1) {
			return 0;
		}
		return curMode + 1;
	}

	public String getCharsetType(Resource resource, int type) throws IOException {
		return getCharsetType(resource, resource, type);
	}

	public String getCharsetType(Resource httpHeadersResource,
			Resource payloadResource, int type) throws IOException {
		if(type == 0) {
			return getCharsetFromHeaders(httpHeadersResource);
		} else if(type == 1) {
			return getCharsetFromMeta(payloadResource);
		} else if(type == 2) {
			return getCharsetFromBytes(payloadResource);
		}
		return null;
	}

	public String getCharset(Resource resource, int mode) throws IOException {
		return getCharset(resource, resource, mode);
	}

	public String getCharset(Resource httpHeadersResource,
			Resource payloadResource, int mode) throws IOException {
		String charset = null;
		if(mode >= MODE_COUNT) {
			mode = 0;
		}
		for(int type = 0; type < GUESS_TYPES; type++) {
			charset = getCharsetType(httpHeadersResource, payloadResource,
					MODES[mode][type]);
			if(charset != null) {
				break;
			}
		}
		if(charset == null) {
			charset = DEFAULT_CHARSET;
		}
		return charset;
	}

	@Override
	public String getCharset(Resource httpHeadersResource,
			Resource payloadResource, WaybackRequest request)
					throws IOException {
		int mode = request.getCharsetMode();
		return getCharset(httpHeadersResource, payloadResource, mode);
	}
}
