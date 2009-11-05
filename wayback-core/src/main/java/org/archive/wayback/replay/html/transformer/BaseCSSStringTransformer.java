/* URLStringTransformer
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
			int urlLength = url.length();
			String replayUrl = context.contextualizeUrl(url, flags);
			int delta = replayUrl.length() - urlLength;
			sb.replace(urlStart, urlStart + urlLength, replayUrl);
			idx += delta;
		}
	}
}
