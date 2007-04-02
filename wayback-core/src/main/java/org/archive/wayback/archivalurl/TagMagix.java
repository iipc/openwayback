/* TagMagix
 *
 * $Id$
 *
 * Created on 5:17:27 PM Feb 14, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.archivalurl;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.SearchResult;

/**
 * Library for updating arbitrary attributes in arbitrary tags to rewrite
 * HTML documents so URI references point back into the Wayback Machine.
 * Attempts to make minimal changes so nothing gets broken during this process.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TagMagix {

	private static HashMap<String,Pattern> pcPatterns = 
		new HashMap<String,Pattern>();
	
	 
	private static String QUOTED_ATTR_VALUE= "(?:\"[^\">]*\")";
	private static String ESC_QUOTED_ATTR_VALUE= "(?:\\\\\"[^>\\\\]*\\\\\")";
	private static String APOSED_ATTR_VALUE = "(?:'[^'>]*')";
	private static String RAW_ATTR_VALUE = "(?:[^ \\t\\n\\x0B\\f\\r>\"']+)";
	
	private static String ANY_ATTR_VALUE = QUOTED_ATTR_VALUE+ "|" + APOSED_ATTR_VALUE +
		"|" + ESC_QUOTED_ATTR_VALUE + "|" + RAW_ATTR_VALUE;
	
	/**
	 * get (and cache) a regex Pattern for locating an HTML attribute value
	 * within a particular tag. if found, the pattern will have the attribute
	 * value in group 1. Note that the attribute value may contain surrounding
	 * apostrophe(') or quote(") characters.
	 * 
	 * @param tagName
	 * @param attrName
	 * @return Pattern to match the tag-attribute's value
	 */
	private synchronized static Pattern getPattern(String tagName, String attrName) {

		String key = tagName + "    " + attrName;
		Pattern pc = pcPatterns.get(key);
		if(pc == null) {
			
			String tagPatString = "<\\s*" + tagName + "\\s+[^>]*\\b" + attrName + 
				"\\s*=\\s*(" + ANY_ATTR_VALUE + ")(?:\\s|>)?";
			
			pc = Pattern.compile(tagPatString,Pattern.CASE_INSENSITIVE);
			pcPatterns.put(key,pc);
		}
		return pc;
	}

	/**
	 * Alter the HTML document in page, updating URLs in the attrName 
	 * attributes of all tagName tags such that:
	 * 
	 *   1) absolute URLs are prefixed with:
	 *         wmPrefix + pageTS
	 * 	 2) server-relative URLs are prefixed with:
	 *         wmPrefix + pageTS + (host of page)
	 *   3) path-relative URLs are prefixed with:
	 *         wmPrefix + pageTS + (attribute URL resolved against pageUrl)
	 * 
	 * @param page
	 * @param uriConverter 
	 * @param result 
	 * @param baseUrl 
	 * @param tagName
	 * @param attrName
	 */
	public static void markupTagREURIC (StringBuilder page, ResultURIConverter uriConverter, 
			SearchResult result, String baseUrl, String tagName, String attrName) {

		Pattern tagPat = getPattern(tagName, attrName);
		Matcher matcher = tagPat.matcher(page);
		
		int idx = 0;
		while(matcher.find(idx)) {
			String url = matcher.group(1);
			int origUrlLength = url.length();
			int attrStart = matcher.start(1);
			int attrEnd = matcher.end(1);
			String quote = "";
			if(url.charAt(0) == '"') {
				quote = "\"";
				url = url.substring(1,url.length()-1);
			} else if(url.charAt(0) == '\'') {
				quote = "'";
				url = url.substring(1,url.length()-1);
			} else if(url.charAt(0) == '\\') {
				quote = "\\\"";
				url = url.substring(2,url.length()-2);
			}

			String replayUrl = quote + uriConverter.makeRedirectReplayURI(
					result, url, baseUrl) + quote;
			
			int delta = replayUrl.length() - origUrlLength;
			page.replace(attrStart,attrEnd,replayUrl);
			idx = attrEnd + delta;
		}
	}

	/**
	 * find and return the href value within a BASE tag inside the HTML document
	 * within the StringBuffer page. returns null if no BASE-HREF is found.
	 * 
	 * @param page
	 * @return URL of base-href within page, or null if none is found.
	 */
	public static String getBaseHref(StringBuilder page) {
		String found = null;
		Pattern baseHrefPattern = TagMagix.getPattern("BASE","HREF");
		Matcher matcher = baseHrefPattern.matcher(page);
		int idx = 0;

		if(matcher.find(idx)) {
			found = matcher.group(1);
			if(found.charAt(0) == '"') {
				found = found.substring(1,found.length()-1);
			} else if(found.charAt(0) == '\'') {
				found = found.substring(1,found.length()-1);
			}
		}

		return found;
	}
	

	
}
