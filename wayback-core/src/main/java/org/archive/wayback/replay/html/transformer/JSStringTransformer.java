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
import org.archive.wayback.replay.html.StringTransformer;

/**
 * Translates absolute URLs found in JavaScript code block.
 * <p>Looks for http/https absolute URLs in JavaScript code and translates
 * them with {@link ReplayParseContext#contextualizeUrl(String)}.</p>
 * <p>TODO: org.archive.wayback.archivalurl.ArchivalUrlJSReplayRenderer has
 * similar code.  can be consolidated, like ArchivalURLJSStringTransformerReplayRenderer?</p>
 * @author brad
 *
 */
public class JSStringTransformer implements StringTransformer {
	private final static Pattern defaultHttpPattern = Pattern
	.compile("(https?:\\\\?/\\\\?/[A-Za-z0-9:_@.-]+)");
	
	private Pattern pattern = defaultHttpPattern;
	
	public void setRegex(String regex)
	{
		pattern = Pattern.compile(regex);
	}
	
	public String getRegex()
	{
		return pattern.pattern();
	}

	public String transform(ReplayParseContext context, String input) {
		StringBuffer replaced = new StringBuffer(input.length());
		Matcher m = pattern.matcher(input);
		while (m.find()) {
			String url = m.group(1);
			if (context.isRewriteSupported(url)) {
				String origUrl = url;
				url = context.contextualizeUrl(url);

				if ((url != null) && (origUrl != null)) {
					// reverse some changes made to url by contextualizeUrl method, that
					// may break assumptions in subsequent JavaScript processing.
					// eg. "http://example.org" -> "/20140101012345/http://example.org/"
					// eg. "https://domain" + ".example.org" -> "http://domain/" + ".example.org"
					// eg. "https://domain." + "example.org" -> "http://domain" + "example.org"

					// remove trailing "/" if origUrl doesn't have it.  As Wayback does not need
					// trailing slash, it may make sense to this everywhere.  Just doing this fix
					// in JavaScript for now.
					if (url.endsWith("/") && !origUrl.endsWith("/")) {
						url = url.substring(0, url.length() - 1);
					}

					// add trailing "." (removed by canonicalizer) back, if origUrl has it.
					if (origUrl.endsWith(".") && !url.endsWith(".")) {
						url = url + ".";
					}
				}
			}
			m.appendReplacement(replaced, url);
		}
		m.appendTail(replaced);
		return replaced.toString();
	}
}
