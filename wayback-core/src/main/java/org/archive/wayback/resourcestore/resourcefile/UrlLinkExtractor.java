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
package org.archive.wayback.resourcestore.resourcefile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.util.ByteOp;


/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlLinkExtractor {
    private final static String QUOTED_ATTR_VALUE = "(?:\"[^\">]*\")";

    private final static String ESC_QUOTED_ATTR_VALUE = "(?:\\\\\"[^>\\\\]*\\\\\")";

    private final static String APOSED_ATTR_VALUE = "(?:'[^'>]*')";

    private final static String RAW_ATTR_VALUE = "(?:[^ \\t\\n\\x0B\\f\\r>\"']+)";


    private final static String ANY_ATTR_VALUE = QUOTED_ATTR_VALUE + "|"
                    + APOSED_ATTR_VALUE + "|" + ESC_QUOTED_ATTR_VALUE + "|"
                    + RAW_ATTR_VALUE;

    private final static String tagName = "a";
    private final static String attrName = "href";

    private final static String tagPatString = "<\\s*" + tagName +
    	"\\s+[^>]*\\b" + attrName + 
    	"\\s*=\\s*(" + ANY_ATTR_VALUE + ")(?:\\s|>)?";

    private final static Pattern pc = Pattern.compile(tagPatString, 
    		Pattern.CASE_INSENSITIVE);

	public static List<String> extractLinks(final String url) throws IOException {
		URL u = new URL(url);
		InputStream is = u.openStream();
		InputStreamReader isr = new InputStreamReader(is,ByteOp.UTF8);
		StringBuilder sb = new StringBuilder(2000);
		int READ_SIZE = 2048;
		char cbuf[] = new char[READ_SIZE];
		int amt = 0;
		while((amt = isr.read(cbuf, 0, READ_SIZE)) != -1) {
			sb.append(new String(cbuf,0,amt));
		}
		return extractAnchors(sb);
	}
	
	private static List<String> extractAnchors(final StringBuilder sb) {
		
		Matcher m = pc.matcher(sb);
		
		ArrayList<String> anchors = new ArrayList<String>();
		int idx = 0;
		while(m.find(idx)) {
            anchors.add(trimAttr(m.group(1)));
            idx = m.end(1);
		}
		return anchors;
	}

	private static String trimAttr(final String attr) {
		int attrLength = attr.length();
        if (attr.charAt(0) == '"') {
        	return attr.substring(1, attrLength - 1);
        } else if (attr.charAt(0) == '\'') {
        	return attr.substring(1, attrLength - 1);
        } else if (attr.charAt(0) == '\\') {
        	return attr.substring(2, attrLength - 2);
        }
        return attr;
	}

}
