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
 * {@link StringTransformer} that rewrites URLs in CSS in <code>STYLE</code>
 * attribute.
 *
 * @see org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler
 * @see BlockCSSStringTransformer
 *
 */
public class InlineCSSStringTransformer extends BaseCSSStringTransformer implements StringTransformer {

	public String transform(ReplayParseContext context, String css) {
		StringBuilder sb = new StringBuilder(css);
		patternRewrite(context, sb, cssUrlPattern, "im_");
		return sb.toString();
	}

}
