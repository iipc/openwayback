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
 * Attempts to rewrite any absolute URLs found within the text/javascript MIME
 * 
 * @author brad
 *
 */
public class JSStringTransformer implements StringTransformer {
	private final static Pattern httpPattern = Pattern
	.compile("(https?:\\\\?/\\\\?/[A-Za-z0-9:_@.-]+)");

	public String transform(ReplayParseContext context, String input) {

		StringBuffer replaced = new StringBuffer(input.length());
		Matcher m = httpPattern.matcher(input);
		while (m.find()) {
			String host = m.group(1);
			m.appendReplacement(replaced, context.contextualizeUrl(host));
		}
		m.appendTail(replaced);
		return replaced.toString();
	}
}
