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
import org.archive.wayback.replay.html.StringBuilderTransformer;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * StringTransformer for translating URLs found in &lt;STYLE&gt; element.
 *
 * @see org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler
 * @see InlineCSSStringTransformer
 */
public class BlockCSSStringTransformer extends BaseCSSStringTransformer
		implements StringTransformer, StringBuilderTransformer {

	public String transform(ReplayParseContext context, String css) {
		StringBuilder sb = new StringBuilder(css);
		transform(context, sb);
		return sb.toString();
	}

	@Override
	public void transform(ReplayParseContext context, StringBuilder text) {
		patternRewrite((ReplayParseContext)context, text, cssUrlPattern, "im_");
		patternRewrite((ReplayParseContext)context, text, cssImportNoUrlPattern,
			"cs_");
	}
}
