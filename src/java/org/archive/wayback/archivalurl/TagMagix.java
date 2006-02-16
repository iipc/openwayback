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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TagMagix {

	/**
	 * Using hope, rythm, and prayer techniques to alter the HTML
	 * document in page, updating URLs in the attrName attributes of all
	 * tagName tags such that:
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
	public static void markupTag(StringBuffer page, String wmPrefix, 
			String pageUrl, String pageTS, String tagName, String attrName) {
		
		UURI pageURI;
		String pageHost;
		try {
			pageURI = UURIFactory.getInstance(pageUrl);
			pageHost = pageURI.getHost();
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String absPrefix = wmPrefix + pageTS + "/";
		String srvPrefix = wmPrefix + pageTS + "/" + pageHost;

		int idx = 0;
		String lcTag = "<" + tagName.toLowerCase();
		String ucTag = "<" + tagName.toUpperCase();
		String ucAttr = attrName.toUpperCase();
		String lcAttr = attrName.toLowerCase();
		
		while(true) {
			int tagStart = page.indexOf(ucTag,idx);
			if(tagStart == -1) {
				tagStart = page.indexOf(lcTag,idx);
			}
			if(tagStart == -1) {
				break;
			}
			// where does the tag end?
			int tagEnd = page.indexOf(">",tagStart);
			// remember where we are:
			idx = tagEnd;
			int attrStart = page.indexOf(ucAttr,tagStart);
			if(attrStart == -1 || attrStart > tagEnd) {
				attrStart = page.indexOf(lcAttr,tagStart);
			}
			if(attrStart == -1 || attrStart > tagEnd) {
				continue;
			}

			// OK, we have the right attribute, on the right tag. 
			// if we can just extract out the value, we'll be set.
			// well, nearly set, anyways...
			
			int attrEnd = attrStart + attrName.length();
			// where is the next '='?
			int equalsIdx = page.indexOf("=",attrEnd);
			if(equalsIdx == -1 || equalsIdx > tagEnd) {
				continue;
			}
			int attrValueStart = -1;
			while(equalsIdx < tagEnd) {
				// scan until we hit the first non-blank
				if(isWhiteSpace(page.charAt(equalsIdx))) {
					equalsIdx++;
					continue;
				}
				attrValueStart = equalsIdx;
				break;
			}
			if(attrValueStart == -1) {
				continue;
			}
			int attrValueEnd = -1;
			char firstChar = page.charAt(attrValueStart);
			if(firstChar == '"') {
				attrValueStart++;
				int attrIdx = attrValueStart;
				// scan until we pass tagEnd or hit another '"':
				while(attrIdx < tagEnd) {
					if(page.charAt(attrIdx) == '"') {
						attrValueEnd = attrIdx - 1;
						break;
					}
					attrIdx++;
				}
			} else if(firstChar == '\'') {
				attrValueStart++;
				int attrIdx = attrValueStart;
				// scan until we pass tagEnd or hit another '\'':
				while(attrIdx < tagEnd) {
					if(page.charAt(attrIdx) == '\'') {
						attrValueEnd = attrIdx - 1;
						break;
					}
					attrIdx++;
				}
				
			} else {
				int attrIdx = attrValueStart;
				// scan until we hit the next whitespace:
				while(attrIdx < tagEnd) {
					if(isWhiteSpace(page.charAt(attrIdx))) {
						attrValueEnd = attrIdx - 1;
						break;
					}
					attrIdx++;
				}
			}
			// did we find the end of the attribute?
			if(attrValueEnd == -1) {
				// nope.
				continue;
			}
			// yes! is the attribute value non-zero length?
			int attrLength = attrValueEnd - attrValueStart;
			if(attrLength < 1) {
				// doh. forget it...
				continue;
			}
			
			// alright. current Attribute value is:
			String attrValue = page.substring(attrValueStart,attrValueEnd);
			
			String newAttrValue = null;

			// is it server-relative (starts with /)?
			if(attrValue.charAt(0) == '/') {
				newAttrValue = srvPrefix + attrValue;
			} else {
			
				
				// try to make a URI out of it:
				UURI attrURI;
				try {
					attrURI = UURIFactory.getInstance(attrValue);
				} catch (URIException e) {
					continue;
					//e.printStackTrace();
				}
				if(attrURI.isAbsoluteURI()) {
					newAttrValue = absPrefix + attrValue;
				} else {
					// assume a path-relative URL:
					try {
						newAttrValue = pageURI.resolve(attrValue).toString();
					} catch (URIException e) {
						continue;
					}
				}
			}

			// WOW! We have an actual replacement for the bastard attribute:
			int delta = attrValue.length() - newAttrValue.length();
			idx += delta;
			page.replace(attrValueStart,attrValueEnd,newAttrValue);
			
		}
	}
	private static boolean isWhiteSpace(char c) {
		return (c == ' ') || (c == '\t') || (c == '\n');
	}
	
	public static void markupTagRE (StringBuffer page, String wmPrefix, 
			String pageUrl, String pageTS, String tagName, String attrName) {

		UURI pageURI;
		String pageHost;
		try {
			pageURI = UURIFactory.getInstance(pageUrl);
			pageHost = pageURI.getHost();
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		String absPrefix = wmPrefix + pageTS + "/";
		String srvPrefix = wmPrefix + pageTS + "/" + pageHost;
		
		String quotedAttrValue = "(\"[^\">]*\")";
		String aposedAttrValue = "('[^'>]*')";
		String rawAttrValue = "([^ \\t\\n\\x0B\\f\\r>\"']+)";
		
		String anyAttrValue = quotedAttrValue + "|" + aposedAttrValue +
//			"";
			"|" + rawAttrValue;
		
		String tagPatString = "<\\s*" + tagName + "\\s+[^>]*\\b" + attrName + 
			"\\s*=\\s*(" + anyAttrValue + ")(\\s|>)?";
		
		Pattern tagPat = Pattern.compile(tagPatString,Pattern.CASE_INSENSITIVE);
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
			String before = page.toString();
			page.replace(attrStart,attrEnd,newValue);
			String after = page.toString();
			System.out.println("Found match (" + before+ ") => (" + after + ")");
			idx = attrEnd + delta;
		}
	}
	private static String resolveAttribute(String value,String abs, String srv, UURI uri) {
		if(value.charAt(0) == '/') {
			// server-relative:
			return srv + value;
			
		} else {
			UURI attrUri;
			try {
				attrUri = UURIFactory.getInstance(value);
			} catch (URIException e1) {
				e1.printStackTrace();
				// TODO: this is giving up -- should not get here..
				return abs + value;
			}
			if(attrUri.isAbsoluteURI()) {
			
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
