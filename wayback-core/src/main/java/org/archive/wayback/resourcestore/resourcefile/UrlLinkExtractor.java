/* UrlLinkExtractor
 *
 * $Id$
 *
 * Created on 4:26:53 PM Jun 5, 2008.
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
package org.archive.wayback.resourcestore.resourcefile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
		InputStreamReader isr = new InputStreamReader(is);
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
