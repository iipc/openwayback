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
 * @author brad
 *
 */
public class MetaRefreshUrlStringTransformer extends URLStringTransformer 
implements StringTransformer {

	private final static Pattern refreshURLPattern = 
		Pattern.compile("^[\\d.]+\\s*;\\s*url\\s*=\\s*(.+?)\\s*$",
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.html.StringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)
	 */
	public String transform(ReplayParseContext context, String input) {
		/*
		 <META 
          HTTP-EQUIV="Refresh"
          CONTENT="0; URL=/ics/default.asp">
          
          Our argument "input" is set to the value of the "CONTENT" attribute.
          
          So, we need to search for the "URL=", take everything to the right
          of that, trim it, contextualize it, and return that.
		 */
		Matcher m = refreshURLPattern.matcher(input);
		if(m.matches()) {
			if(m.groupCount() == 1) {
				StringBuilder sb = new StringBuilder(input.length() * 2);
	
				sb.append(input.substring(0,m.start(1)));

				sb.append(super.transform(context, m.group(1)));
				
				// This was temporarily used for testing the regex:
//				sb.append("(((").append(m.group(1)).append(")))");
				
				sb.append(input.substring(m.end(1)));
				return sb.toString();
			}
		}
		return input;
	}

}
