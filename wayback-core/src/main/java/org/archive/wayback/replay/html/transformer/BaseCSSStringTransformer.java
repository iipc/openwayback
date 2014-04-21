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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.replay.html.ReplayParseContext;

public abstract class BaseCSSStringTransformer {
	// this looks for "url(ZZZ)"
	protected static String cssUrlPatString = 
		"url\\s*\\(\\s*([\\\\\"']*.+?[\\\\\"']*)\\s*\\)";
//	protected static String cssUrlPatString = 
//		"url\\s*\\(\\s*([^\\)]*)\\s*\\)";

	// this looks for various forms of "@import ZZZ" where "ZZZ" may or may not
	// have quotes and parenths around it..
	// this regex is not supposed to match the (correct) @import url(ZZZ) form,
	// which is handled by the more generic "url(ZZZ)" pattern
	protected static String cssImportNoUrlPatString = 
		"@import\\s+(('[^']+')|(\"[^\"]+\")|(\\('[^']+'\\))|(\\(\"[^\"]+\"\\))|(\\([^)]+\\))|([a-z0-9_.:/\\\\-]+))\\s*;";

	protected static Pattern cssImportNoUrlPattern = Pattern
			.compile(cssImportNoUrlPatString);

	protected static Pattern cssUrlPattern = Pattern.compile(cssUrlPatString);

	protected void patternRewrite(ReplayParseContext context, StringBuilder sb,
			Pattern pattern, String flags) {
		int idx = 0;
		Matcher urlMatcher = pattern.matcher(sb);
		while (urlMatcher.find(idx)) {
			String url = urlMatcher.group(1);
			int origUrlLength = url.length();
			int urlStart = urlMatcher.start(1);
			int urlEnd = urlMatcher.end(1);
			idx = urlEnd;
			if ((url.charAt(0) == '(') 
					&& (url.charAt(origUrlLength-1) == ')')) {
				url = url.substring(1, origUrlLength - 1);
				urlStart += 1;
				origUrlLength -= 2;
			}
			if (url.charAt(0) == '"') {
				url = url.substring(1, origUrlLength - 1);
				urlStart += 1;
			} else if (url.charAt(0) == '\'') {
				url = url.substring(1, origUrlLength - 1);
				urlStart += 1;
			} else if (url.charAt(0) == '\\') {
				url = url.substring(2, origUrlLength - 2);
				urlStart += 2;
			}
			if (context.isRewriteSupported(url)) {
				int urlLength = url.length();
				String replayUrl = context.contextualizeUrl(url, flags);
				int delta = replayUrl.length() - urlLength;
				sb.replace(urlStart, urlStart + urlLength, replayUrl);
				// adjust start of next match as URL extends
				idx += delta;
			}
		}
	}
}
