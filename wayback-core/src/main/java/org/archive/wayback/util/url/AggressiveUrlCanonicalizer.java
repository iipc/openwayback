/* UrlCanonicalizer
 *
 * $Id$
 *
 * Created on 2:08:07 PM Oct 11, 2006.
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
package org.archive.wayback.util.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * Class that performs the standard Heritrix URL canonicalization. Eventually,
 * this should all be configurable, or perhaps be able to read the settings
 * used within a Heritrix crawler... or even multiple crawlers... this is hard.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AggressiveUrlCanonicalizer {

	
	private static final String CDX_PREFIX = " CDX ";
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
    	                 "[0-9a-zA-Z]{32})(?:&(.*))?$",  
    	                 Pattern.CASE_INSENSITIVE);

    /**
     * Example: sid=9682993c8daa2c5497996114facdc805. 
     * 'sid=' can be tricky but all sid= followed by 32 byte string
     * so far seen have been session ids.  Sid is a 32 byte string
     * like the BASE_PATTERN only 'sid' is the tail of 'phpsessid'
     * so have to have it run after the phpsessid elimination.
     */
    private static final Pattern STRIP_SID_REGEX =
        Pattern.compile("^(.+)" +
                "(?:sid=[0-9a-zA-Z]{32})(?:&(.*))?$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Example:ASPSESSIONIDAQBSDSRT=EOHBLBDDPFCLHKPGGKLILNAM.
     */
    private static final Pattern STRIP_ASPSESSION_REGEX =
        Pattern.compile("^(.+)" +
                "(?:ASPSESSIONID[a-zA-Z]{8}=[a-zA-Z]{24})(?:&(.*))?$",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Examples:
     *
     *        (.NET 2.0)
     *        http://legislature.mi.gov/(S(4hqa0555fwsecu455xqckv45))/mileg.aspx
     *     => http://legislature.mi.gov/mileg.aspx
     *
     *		  (.NET 1.0/1.1)
     *        http://legislature.mi.gov/(4hqa0555fwsecu455xqckv45)/mileg.aspx
     *     => http://legislature.mi.gov/mileg.aspx
     *     
     *     For more info, see: 
     *     	  http://msdn2.microsoft.com/en-us/library/aa479315.aspx
     *     
     */
    private static final Pattern STRIP_ASPSESSION2_REGEX =
    	Pattern.compile("^([^\\?]+/)" +
    			"(?:\\((?:S\\(|)[0-9a-z]{24}\\)(?:\\)|)/)([^\\?]+\\.aspx.*)$",
    			Pattern.CASE_INSENSITIVE);
    
    /**
     * Examples:
     *
     *        (.NET 2.0)
     *        http://legislature.mi.gov/(a(4hqa0555fwsecu455xqckv45)S(4hqa0555fwsecu455xqckv45)f(4hqa0555fwsecu455xqckv45))/mileg.aspx?page=SessionSchedules
     *     => http://legislature.mi.gov/(a(4hqa0555fwsecu455xqckv45)f(4hqa0555fwsecu455xqckv45))/mileg.aspx?page=SessionSchedules
     *
     *     For more info, see: 
     *     	  http://msdn2.microsoft.com/en-us/library/aa479315.aspx
     *     
     */   

    private static final Pattern STRIP_ASPSESSION3_REGEX =
    	Pattern.compile("^([^\\?]+/" +
    			"\\((?:a\\([0-9a-z]{24}\\)))(?:S\\([0-9a-z]{24}\\))" +
    			"((?:f\\([0-9a-z]{24}\\))\\)/[^\\?]+\\.aspx.*)$",
    			Pattern.CASE_INSENSITIVE);
    
    /**
     * Strip ColdFusion session IDs. Remove sessionids that look like the 
     * following:
     * CFID=12412453&CFTOKEN=15501799
     * CFID=3304324&CFTOKEN=57491900&jsessionid=a63098d96360$B0$D9$A
     */
    private static final Pattern STRIP_CFSESSION_REGEX = 
    	Pattern.compile("^(.+)(?:cfid=[^&]+&cftoken=[^&]+(?:jsession=[^&]+)?)" +
    			"(?:&(.*))?$",Pattern.CASE_INSENSITIVE);
        
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
	 * return the canonical string key for the URL argument.
	 * 
	 * @param urlString
	 * @return String lookup key for URL argument.
	 * @throws URIException 
	 */
	public String urlStringToKey(final String urlString) throws URIException {

		String searchUrl = canonicalize(urlString);

		// TODO: force https into http for the moment...
		if(searchUrl.startsWith("https://")) {
			searchUrl = searchUrl.substring(8);
		}
		
		// TODO: this will only work with http:// scheme. should work with all?
		// force add of scheme and possible add '/' with empty path:
		if (searchUrl.startsWith("http://")) {
			if (-1 == searchUrl.indexOf('/', 8)) {
				searchUrl = searchUrl + "/";
			}
		} else {
			if (-1 == searchUrl.indexOf("/")) {
				searchUrl = searchUrl + "/";
			}
			searchUrl = "http://" + searchUrl;
		}

		// unescape anythying that can be:
		UURI tmpURI = UURIFactory.getInstance(searchUrl);
		tmpURI.setPath(tmpURI.getPath());
		
		
		// convert to UURI to perform require URI fixup:
		UURI searchURI = UURIFactory.getInstance(tmpURI.getURI());


		
		
		// replace ' ' with '+' (this is only to match Alexa's canonicalization)
		String newPath = searchURI.getEscapedPath().replace("%20","+");
//		String newPath = searchURI.getPath().replace(' ','+');
		
		// replace multiple consecutive '/'s in the path.
		while(newPath.contains("//")) {
			newPath = newPath.replace("//","/");
		}
		
		// this would remove trailing a '/' character, unless the path is empty
		// but we're not going to do this just yet..
//		if((newPath.length() > 1) && newPath.endsWith("/")) {
//			newPath = newPath.substring(0,newPath.length()-1);
//		}
//		searchURI.setEscapedPath(newPath);
//		searchURI.setRawPath(newPath.toCharArray());
//		String query = searchURI.getEscapedQuery();
		
		// TODO: handle non HTTP port stripping, too.
//		String portStr = "";
//		if(searchURI.getPort() != 80 && searchURI.getPort() != -1) {
//			portStr = ":" + searchURI.getPort();
//		}
//		return searchURI.getHostBasename() + portStr + 
//		searchURI.getEscapedPathQuery();
		
		StringBuilder sb = new StringBuilder(searchUrl.length());
		sb.append(searchURI.getHostBasename());
		if(searchURI.getPort() != 80 && searchURI.getPort() != -1) {
			sb.append(":").append(searchURI.getPort());
		}
		sb.append(newPath);
		if(searchURI.getEscapedQuery() != null) {
			sb.append("?").append(searchURI.getEscapedQuery());
		}
		

		return sb.toString();
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
        url = doStripRegexMatch(url, STRIP_ASPSESSION2_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_ASPSESSION3_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_SID_REGEX.matcher(url));
        url = doStripRegexMatch(url, STRIP_CFSESSION_REGEX.matcher(url));
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
	
	private static void USAGE() {
		System.err.println("Usage: [-f FIELD] [-d DELIM]");
		System.exit(3);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AggressiveUrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
		int n = 0;
		int i = 0;
		ArrayList<Integer> columns = new ArrayList<Integer>();
		
		long lineNumber = 0;
		boolean cdxPassThru = false;
		String delimiter = " ";
		while(n < args.length) {
			String arg = args[n];
			if(arg.compareTo("-cdx") == 0) {
				cdxPassThru = true;
				n++;
				continue;
			}
			if(n == (args.length -1)) {
				USAGE();
			}
			String val = args[n+1];
			if(arg.compareTo("-f") == 0) {
				columns.add(new Integer(val));
			} else if(arg.compareTo("-d") == 0) {
				delimiter = val;
			} else {
				USAGE();
			}
			n += 2;
		}
		// place default '0' in case none specified:
		if(columns.size() == 0) {
			columns.add(new Integer(1));
		}
		
		// convert to int[]:
		int[] cols = new int[columns.size()];
		for(int idx = 0; idx < columns.size(); idx++) {
			cols[idx] = columns.get(idx).intValue() - 1;
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		while(true) {
			try {
				line = r.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if(line == null) {
				break;
			}
			lineNumber++;
			if(cdxPassThru && line.startsWith(CDX_PREFIX)) {
				System.out.println(line);
				continue;
			}
			String parts[] = line.split(delimiter);
			for(int column : cols) {
				if(column >= parts.length) {
					System.err.println("Invalid line " + lineNumber + " (" +
							line + ") skipped");
				} else {
					try {
						parts[column] = canonicalizer.urlStringToKey(parts[column]);
					} catch (URIException e) {
						System.err.println("Invalid URL in line " + lineNumber + " (" +
								line + ") skipped (" + parts[column] + ")");
						e.printStackTrace();
						continue;
					}
				}
			}
			sb.setLength(0);
			for(i = 0; i < parts.length; i++) {
				sb.append(parts[i]);
				if(i < (parts.length-1)) {
					sb.append(delimiter);
				}
			}
			System.out.println(sb.toString());
		}
	}
}