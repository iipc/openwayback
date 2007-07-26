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
package org.archive.wayback.replay;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.util.UrlCanonicalizer;

/**
 * Library for updating arbitrary attributes in arbitrary tags to rewrite HTML
 * documents so URI references point back into the Wayback Machine. Attempts to
 * make minimal changes so nothing gets broken during this process.
 * 
 * @author brad
 * @version $Date$, $Revision:
 *          1668 $
 */
public class TagMagix {

	private static HashMap<String, Pattern> pcPatterns = 
		new HashMap<String, Pattern>();

	private static HashMap<String, Pattern> wholeTagPatterns = 
		new HashMap<String, Pattern>();

	private static HashMap<String, Pattern> attrPatterns = 
		new HashMap<String, Pattern>();

	private static String QUOTED_ATTR_VALUE = "(?:\"[^\">]*\")";

	private static String ESC_QUOTED_ATTR_VALUE = "(?:\\\\\"[^>\\\\]*\\\\\")";

	private static String APOSED_ATTR_VALUE = "(?:'[^'>]*')";

	private static String RAW_ATTR_VALUE = "(?:[^ \\t\\n\\x0B\\f\\r>\"']+)";

	private static String ANY_ATTR_VALUE = QUOTED_ATTR_VALUE + "|"
			+ APOSED_ATTR_VALUE + "|" + ESC_QUOTED_ATTR_VALUE + "|"
			+ RAW_ATTR_VALUE;

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
	private synchronized static Pattern getPattern(String tagName,
			String attrName) {

		String key = tagName + "    " + attrName;
		Pattern pc = pcPatterns.get(key);
		if (pc == null) {

			String tagPatString = "<\\s*" + tagName + "\\s+[^>]*\\b" + attrName
					+ "\\s*=\\s*(" + ANY_ATTR_VALUE + ")(?:\\s|>)?";

			pc = Pattern.compile(tagPatString, Pattern.CASE_INSENSITIVE);
			pcPatterns.put(key, pc);
		}
		return pc;
	}

	/**
	 * get (and cache) a regex Pattern for locating an entire HTML start tag.
	 * 
	 * @param tagName
	 * @return Pattern to match the tag
	 */
	private synchronized static Pattern getWholeTagPattern(String tagName) {

		Pattern pc = wholeTagPatterns.get(tagName);
		if (pc == null) {

			String tagPatString = "<\\s*" + tagName + "\\s+[^>]+>";

			pc = Pattern.compile(tagPatString, Pattern.CASE_INSENSITIVE);
			wholeTagPatterns.put(tagName, pc);
		}
		return pc;
	}

	/**
	 * get (and cache) a regex Pattern for locating an attribute value within an
	 * HTML start tag. If this pattern matches, the attribute value will be in
	 * group(1), and will include surrounding quotes, or apos, if they were
	 * present in the original HTML.
	 * 
	 * @param attrName
	 * @return Pattern to match the attributes value
	 */
	private synchronized static Pattern getAttrPattern(String attrName) {

		Pattern pc = attrPatterns.get(attrName);
		if (pc == null) {

			String attrPatString = "\\b" + attrName + "\\s*=\\s*("
					+ ANY_ATTR_VALUE + ")(?:\\s|>)?";

			pc = Pattern.compile(attrPatString, Pattern.CASE_INSENSITIVE);
			attrPatterns.put(attrName, pc);
		}
		return pc;
	}

	/**
	 * Alter the HTML document in page, updating URLs in the attrName attributes
	 * of all tagName tags such that:
	 * 
	 * 1) absolute URLs are prefixed with: wmPrefix + pageTS 2) server-relative
	 * URLs are prefixed with: wmPrefix + pageTS + (host of page) 3)
	 * path-relative URLs are prefixed with: wmPrefix + pageTS + (attribute URL
	 * resolved against pageUrl)
	 * 
	 * @param page
	 * @param uriConverter
	 * @param captureDate
	 * @param baseUrl which must be absolute
	 * @param tagName
	 * @param attrName
	 */
	public static void markupTagREURIC(StringBuilder page,
			ResultURIConverter uriConverter, String captureDate,
			String baseUrl, String tagName, String attrName) {

		Pattern tagPat = getPattern(tagName, attrName);
		Matcher matcher = tagPat.matcher(page);

		int idx = 0;
		while (matcher.find(idx)) {
			String url = matcher.group(1);
			int origUrlLength = url.length();
			int attrStart = matcher.start(1);
			int attrEnd = matcher.end(1);
			String quote = "";
			if (url.charAt(0) == '"') {
				quote = "\"";
				url = url.substring(1, url.length() - 1);
			} else if (url.charAt(0) == '\'') {
				quote = "'";
				url = url.substring(1, url.length() - 1);
			} else if (url.charAt(0) == '\\') {
				quote = "\\\"";
				url = url.substring(2, url.length() - 2);
			}
			String finalUrl = UrlCanonicalizer.resolveUrl(baseUrl,url);
			String replayUrl = quote
					+ uriConverter.makeReplayURI(captureDate, finalUrl) + quote;

			int delta = replayUrl.length() - origUrlLength;
			page.replace(attrStart, attrEnd, replayUrl);
			idx = attrEnd + delta;
		}
	}

	private static String trimAttrValue(String value) {
		if (value.charAt(0) == '"') {
			value = value.substring(1, value.length() - 1);
		} else if (value.charAt(0) == '\'') {
			value = value.substring(1, value.length() - 1);
		}
		return value;
	}

	/**
	 * find and return the ATTR value within a TAG tag inside the HTML document
	 * within the StringBuffer page. returns null if no TAG-ATTR is found.
	 * 
	 * @param page
	 * @param tag
	 * @param attr
	 * @return URL of base-href within page, or null if none is found.
	 */
	public static String getTagAttr(StringBuilder page, final String tag,
			final String attr) {

		String found = null;
		Pattern daPattern = TagMagix.getPattern(tag, attr);
		Matcher matcher = daPattern.matcher(page);
		int idx = 0;

		if (matcher.find(idx)) {
			found = matcher.group(1);
			found = trimAttrValue(found);
		}

		return found;
	}

	/**
	 * Search through the HTML contained in page, returning the value of a
	 * particular attribute. This version allows matching only tags that contain
	 * a particular attribute-value pair, which is useful in extracting META tag
	 * values, for example, in returning the value of the "content" attribute in
	 * a META tag that also contains an attribute "http-equiv" with a value of
	 * "Content-Type". All comparision is case-insensitive, but the value
	 * returned is the original attribute value, as unmolested as possible.
	 * 
	 * If nothing matches, returns null.
	 * 
	 * 
	 * @param page
	 *            StringBuilding holding HTML
	 * @param tag
	 *            String containing tagname of interest
	 * @param findAttr
	 *            name of attribute within the tag to return
	 * @param whereAttr
	 *            only match tags with an attribute whereAttr
	 * @param whereVal
	 *            only match tags with whereAttr having this value
	 * @return the value of attribute attr in tag where the tag also contains an
	 *         attribute whereAttr, with value whereVal, or null if nothing
	 *         matches.
	 */
	public static String getTagAttrWhere(StringBuilder page, final String tag,
			final String findAttr, final String whereAttr, final String whereVal) {

		Pattern tagPattern = TagMagix.getWholeTagPattern(tag);
		Pattern findAttrPattern = getAttrPattern(findAttr);
		Pattern whereAttrPattern = getAttrPattern(whereAttr);
		Matcher tagMatcher = tagPattern.matcher(page);

		while (tagMatcher.find()) {
			String wholeTag = tagMatcher.group();
			Matcher whereAttrMatcher = whereAttrPattern.matcher(wholeTag);
			if (whereAttrMatcher.find()) {
				String attrValue = whereAttrMatcher.group(1);
				attrValue = trimAttrValue(attrValue);
				if (attrValue.compareToIgnoreCase(whereVal) == 0) {
					// this tag contains the right set, return the value for
					// the attribute findAttr:
					Matcher findAttrMatcher = findAttrPattern.matcher(wholeTag);
					String value = null;
					if (findAttrMatcher.find()) {
						value = findAttrMatcher.group(1);
						value = trimAttrValue(value);
					}
					return value;
				}
				// not the tag we want... maybe there is another: loop
			}
		}

		return null;
	}

	/**
	 * find and return the href value within a BASE tag inside the HTML document
	 * within the StringBuffer page. returns null if no BASE-HREF is found.
	 * 
	 * @param page
	 * @return URL of base-href within page, or null if none is found.
	 */
	public static String getBaseHref(StringBuilder page) {
		return getTagAttr(page, "BASE", "HREF");
	}
}
