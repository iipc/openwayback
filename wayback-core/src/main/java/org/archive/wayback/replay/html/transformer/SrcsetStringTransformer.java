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
 * {@link StringTransformer} that rewrites URLs in <code>SRCSET</code>
 * attribute.
 *
 */
public class SrcsetStringTransformer implements StringTransformer {

    // this looks for "http://example.com/img/pic.jpg 150w,"
    protected static String SRCSET_MEMBER =
        "\\s*(\\S+)(?:\\s*|\\s+(?:[-+]?[0-9]*\\.?[0-9]+x|[0-9]+w)\\s*)(?:,|$)";

    protected static Pattern srcsetUrlPattern = Pattern.compile(SRCSET_MEMBER);

    protected void patternRewrite(ReplayParseContext context, StringBuilder sb,
            Pattern pattern, String flags) {
        int idx = 0;
        Matcher srcsetMemberMatcher = pattern.matcher(sb);
        while (srcsetMemberMatcher.find(idx)) {
            String url = srcsetMemberMatcher.group(1);
            int urlLength = url.length();
            int urlStart = srcsetMemberMatcher.start(1);
            idx = srcsetMemberMatcher.end();
            String replayUrl = context.contextualizeUrl(url, flags);
            if (replayUrl != url) {
                int delta = replayUrl.length() - urlLength;
                sb.replace(urlStart, urlStart + urlLength, replayUrl);
                // adjust start of next match as URL extends
                idx += delta;
            }
        }
    }

    public String transform(ReplayParseContext context, String srcset) {
        StringBuilder sb = new StringBuilder(srcset);
        patternRewrite(context, sb, srcsetUrlPattern, "im_");
        return sb.toString();
    }
}
