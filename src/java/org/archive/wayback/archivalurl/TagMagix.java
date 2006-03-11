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

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * Library for updating arbitrary attributes in arbitrary tags to rewrite
 * HTML documents so URI references point back into the Wayback Machine.
 * Attempts to make minimal changes so nothing gets broken during this process.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TagMagix {

	private static HashMap pcPatterns = new HashMap();
	
	 
	private static String QUOTED_ATTR_VALUE= "(\"[^\">]*\")";
	private static String APOSED_ATTR_VALUE = "('[^'>]*')";
	private static String RAW_ATTR_VALUE = "([^ \\t\\n\\x0B\\f\\r>\"']+)";
	
	private static String ANY_ATTR_VALUE = QUOTED_ATTR_VALUE+ "|" + APOSED_ATTR_VALUE +
		"|" + RAW_ATTR_VALUE;
	
	private static Pattern getPattern(String tagName, String attrName) {

		String key = tagName + "    " + attrName;
		Pattern pc = (Pattern) pcPatterns.get(key);
		if(pc == null) {
			
			String tagPatString = "<\\s*" + tagName + "\\s+[^>]*\\b" + attrName + 
				"\\s*=\\s*(" + ANY_ATTR_VALUE + ")(\\s|>)?";
			
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
	 * @param wmPrefix
	 * @param pageUrl
	 * @param pageTS 
	 * @param tagName
	 * @param attrName
	 */
	public static void markupTagRE (StringBuffer page, String wmPrefix, 
			String pageUrl, String pageTS, String tagName, String attrName) {

		UURI pageURI;
		String pageHost;
		try {
			if(!pageUrl.startsWith("http://")) {
				pageUrl = "http://" + pageUrl;
			}
			pageURI = UURIFactory.getInstance(pageUrl);
			pageHost = pageURI.getScheme() + "://" + pageURI.getHost();
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String absPrefix = wmPrefix + pageTS + "/";
		String srvPrefix = wmPrefix + pageTS + "/" + pageHost;
				
		Pattern tagPat = getPattern(tagName, attrName);
		Matcher matcher = tagPat.matcher(page);
		
		int idx = 0;
		while(matcher.find(idx)) {
			String attrValue = matcher.group(1);
			int origAttrLength = attrValue.length();
			int attrStart = matcher.start(1);
			int attrEnd = matcher.end(1);
			String quote = "";
			if(attrValue.charAt(0) == '"') {
				quote = "\"";
				attrValue = attrValue.substring(1,attrValue.length()-1);
			} else if(attrValue.charAt(0) == '\'') {
				quote = "'";
				attrValue = attrValue.substring(1,attrValue.length()-1);
			}

			String newValue = quote + resolveAttribute(attrValue,
					absPrefix,srvPrefix,pageURI) + quote;
			
			int delta = newValue.length() - origAttrLength;
			//String before = page.toString();
			page.replace(attrStart,attrEnd,newValue);
			//String after = page.toString();
			//System.out.println("Found match (" + before+ ") => (" + after + ")");
			idx = attrEnd + delta;
		}
	}
	
	private static String resolveAttribute(String value,String abs, String srv, UURI uri) {
		if(value.charAt(0) == '/') {
			// server-relative:
			return srv + value;
			
		} else {
			UURI attrUri = null;
			try {
				attrUri = UURIFactory.getInstance(value);
			} catch (URIException e1) {
				// we can get here if the value is not an absolute URL
				// will be handled below when attrUri is null.
			}
			if(attrUri != null && attrUri.isAbsoluteURI()) {
			
				return abs + value;
			} else {
				try {
					UURI resolved = uri.resolve(value);
					return abs + resolved.toString();
				} catch (URIException e) {
					e.printStackTrace();
				}
				// TODO: this is giving up -- should not get here..
				return abs + value;
			}
		}
	}
}
