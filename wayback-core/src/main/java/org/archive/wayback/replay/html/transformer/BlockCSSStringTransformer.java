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

public class BlockCSSStringTransformer extends BaseCSSStringTransformer implements StringTransformer {

	public String transform(ReplayParseContext context, String css) {
	    if (!context.isRewriteSupported(css)) {
	    	return css;
	    }
		StringBuilder sb = new StringBuilder(css);
		patternRewrite((ReplayParseContext)context, sb,cssUrlPattern, "im_");
		patternRewrite((ReplayParseContext)context, sb,cssImportNoUrlPattern, 
				"cs_");
//		return "__BCSS__" + sb.toString() + "__BCSS__";
		return sb.toString();
	}

}
