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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;

public class XArchiveHttpHeaderProcessor implements HttpHeaderProcessor {

	private static String DEFAULT_PREFIX = "X-Wayback-Orig-";
	private String prefix = DEFAULT_PREFIX; 
	private Set<String> passThrough = null;
	
	public XArchiveHttpHeaderProcessor() {
		passThrough = new HashSet<String>();
		passThrough.add(HTTP_CONTENT_TYPE_HEADER_UP);
		passThrough.add(HTTP_CONTENT_DISP_HEADER_UP);
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {
		String keyUp = key.toUpperCase();

		output.put(prefix + key,value);
		if (passThrough.contains(keyUp)) {
//			if (keyUp.startsWith(HTTP_CONTENT_TYPE_HEADER_UP)) {
			// add this one as-is, too.
			output.put(key, value);
		}
	}
}
