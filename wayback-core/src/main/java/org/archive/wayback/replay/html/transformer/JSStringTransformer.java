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
			String host = m.group(1);
			
			String origHost = host;
			host = context.contextualizeUrl(host);
			
		    // This is a fix for situations for hostnames only being resolved with a trailing slash
		    // eg. http://example.org -> /datestamp/http://example.org/
		    // This will remove the trailing /, as it may break certain javascript, and is not necessary
			// ex 'http://domain' + '.example.org' would get converted to 
			// 'http://domain/.example.org' instead of 'http://domain.example.org' without this fix.
			// Wayback does need the trailing slash at all, and may make sense to change this everywhere
			// for now, just applying this to JS
		    if ((host != null) && (origHost != null) && host.endsWith("/") && !origHost.endsWith("/")) {
		    	host = host.substring(0, host.length() - 1);
		    }
			m.appendReplacement(replaced, host);
		}
		m.appendTail(replaced);
		return replaced.toString();
	}
}
