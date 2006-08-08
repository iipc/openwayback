/* UrlCanonicalizer
 *
 * $Id$
 *
 * Created on 4:06:07 PM Aug 8, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.cdx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlCanonicalizer {
    /**
     * Strip leading 'www.'
     */
    private static final Pattern STRIP_WWW_REGEX =
        Pattern.compile("(?i)^(https?://)(?:www\\.)([^/]*/.+)$");
    /**
     * Strip leading 'www44.', 'www3.', etc.
     */
    private static final Pattern STRIP_WWWN_REGEX =
        Pattern.compile("(?i)^(https?://)(?:www[0-9]+\\.)([^/]*/.+)$");
    /**
     * Strip userinfo.
     */
    private static final Pattern STRIP_USERINFO_REGEX =
        Pattern.compile("^((?:(?:https?)|(?:ftps?))://)(?:[^/]+@)(.*)$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Example: jsessionid=999A9EF028317A82AC83F0FDFE59385A.
     * Example: PHPSESSID=9682993c8daa2c5497996114facdc805.
     */
    private static final Pattern STRIP_SESSION_ID_REGEX =
        Pattern.compile("^(.+)(?:(?:(?:jsessionid)|(?:phpsessid))=" +
                 "[0-9a-zA-Z]{32})(&.*)?$",
            Pattern.CASE_INSENSITIVE);
    
    /**
     * Example: sid=9682993c8daa2c5497996114facdc805. 
     * 'sid=' can be tricky but all sid= followed by 32 byte string
     * so far seen have been session ids.  Sid is a 32 byte string
     * like the BASE_PATTERN only 'sid' is the tail of 'phpsessid'
     * so have to have it run after the phpsessid elimination.
     */
    private static final Pattern STRIP_SID_REGEX =
        Pattern.compile("^(.+)(?:sid=[0-9a-zA-Z]{32})(&.*)?$",
            Pattern.CASE_INSENSITIVE);
    
    /**
     * Example:ASPSESSIONIDAQBSDSRT=EOHBLBDDPFCLHKPGGKLILNAM.
     */
    private static final Pattern STRIP_ASPSESSION_REGEX =
        Pattern.compile("^(.+)(?:ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24})(&.*)?$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Run a regex that strips elements of a string.
     * 
     * Assumes the regex has a form that wants to strip elements of the passed
     * string.  Assumes that if a match, appending group 1
     * and group 2 yields desired result.
     * @param url Url to search in.
     * @param matcher Matcher whose form yields a group 1 and group 2 if a
     * match (non-null.
     * @return Original <code>url</code> else concatenization of group 1
     * and group 2.
     */
    protected String doStripRegexMatch(String url, Matcher matcher) {
        return (matcher != null && matcher.matches())?
            checkForNull(matcher.group(1)) + checkForNull(matcher.group(2)):
            url;
    }

    /**
     * @param string String to check.
     * @return <code>string</code> if non-null, else empty string ("").
     */
    private String checkForNull(String string) {
        return (string != null)? string: "";
    }

	
	/**
	 * Idempotent operation that will determine the 'fuzziest'
	 * form of the url argument. This operation is done prior to adding records
	 * to the ResourceIndex, and prior to lookup. Current version is exactly
	 * the default found in Heritrix. When the configuration system for
	 * Heritrix stabilizes, hopefully this can use the system directly within
	 * Heritrix.
	 * 
	 * @param url to be canonicalized.
	 * @return canonicalized version of url argument.
	 */
	public String canonicalize(String url) {
        url = doStripRegexMatch(url, STRIP_USERINFO_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_WWW_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_WWWN_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_SESSION_ID_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_ASPSESSION_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_SID_REGEX.matcher(url));
        url = url.toLowerCase();
        if (url == null || url.length() <= 0) {
            return url;
        }
        
        int index = url.lastIndexOf('?');
        if (index > 0) {
            if (index == (url.length() - 1)) {
                // '?' is last char in url.  Strip it.
                url = url.substring(0, url.length() - 1);
            } else if (url.charAt(index + 1) == '&') {
                // Next char is '&'. Strip it.
                if (url.length() == (index + 2)) {
                    // Then url ends with '?&'.  Strip them.
                    url = url.substring(0, url.length() - 2);
                } else {
                    // The '&' is redundant.  Strip it.
                    url = url.substring(0, index + 1) +
                    url.substring(index + 2);
                }
            } else if (url.charAt(url.length() - 1) == '&') {
                // If we have a lone '&' on end of query str,
                // strip it.
                url = url.substring(0, url.length() - 1);
            }
        }
        return url;
	}
}
