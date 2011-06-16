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
package org.archive.wayback.replay.html.transformer;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * @author brad
 *
 */
public class URLStringTransformer implements StringTransformer {
	private String flags;
	private StringTransformer jsTransformer = null;
	/** Default constructor */
	public URLStringTransformer() {}
	/** 
	 * Flag-setting constructor 
	 * @param flags flags to pass to ReplayParseContext.contextualizeUrl()
	 */
	public URLStringTransformer(String flags) {
		this.flags = flags;
	}
	
	public String transform(ReplayParseContext context, String url) {
		if(url.startsWith(ReplayParseContext.JAVASCRIPT_PREFIX)) {
			if(jsTransformer == null) {
				return url;
			}
			StringBuilder sb = new StringBuilder(url.length());
			sb.append(ReplayParseContext.JAVASCRIPT_PREFIX);
			String jsFragment = url.substring(
					ReplayParseContext.JAVASCRIPT_PREFIX.length());
			sb.append(jsTransformer.transform(context, jsFragment));
			return sb.toString();
		}
		return context.contextualizeUrl(url, flags);
	}

	/** @return the flags */
	public String getFlags() {
		return flags;
	}

	/** @param flags the flags to set */
	public void setFlags(String flags) {
		this.flags = flags;
	}

	public StringTransformer getJsTransformer() {
		return jsTransformer;
	}
	public void setJsTransformer(StringTransformer jsTransformer) {
		this.jsTransformer = jsTransformer;
	}

}
