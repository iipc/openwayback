/* UrlOperations
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

/**
 * Class containing common static URL methods. Primarily resolveUrl() and 
 * the (currently) unused isAuthority().
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlOperations {
	private static final Logger LOGGER = Logger.getLogger(
			UrlOperations.class.getName());
	
	/**
	 * ARC/WARC specific DNS resolution record.
	 */
	public final static String DNS_SCHEME = "dns:";
	/**
	 * HTTP
	 */
	public final static String HTTP_SCHEME = "http://";
	/**
	 * HTTPS
	 */
	public final static String HTTPS_SCHEME = "https://";
	/**
	 * FTP
	 */
	public final static String FTP_SCHEME = "ftp://";
	/**
	 * MMS
	 */
	public final static String MMS_SCHEME = "mms://";
	/**
	 * RTSP
	 */
	public final static String RTSP_SCHEME = "rtsp://";
	
	/**
	 * Default scheme to assume if unspecified. No context implied...
	 */
	public final static String DEFAULT_SCHEME = HTTP_SCHEME;	
	
	/**
	 * go brewster
	 */
	public final static String WAIS_SCHEME = "wais://";
	
	/**
	 * array of static Strings for all "known" schemes
	 */
	public final static String ALL_SCHEMES[] = { 
		HTTP_SCHEME,
		HTTPS_SCHEME,
		FTP_SCHEME,
		MMS_SCHEME,
		RTSP_SCHEME,
		WAIS_SCHEME
	};
	
	
	/**
	 * character separating host from port within a URL authority
	 */
	public final static char PORT_SEPARATOR = ':';
	/**
	 * character which delimits the path from the authority in a... in some 
	 * URLs.
	 */
	public final static char PATH_START = '/';
	
	
	private static final String CC_TLDS = "ac|ad|ae|af|ag|ai|al|am|an|ao|aq" +
			"|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs" +
			"|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cx" +
			"|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo" +
			"|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk" +
			"|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg" +
			"|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma" +
			"|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz" +
			"|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm" +
			"|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj" +
			"|sk|sl|sm|sn|so|sr|st|su|sv|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn" +
			"|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|um|us|uy|uz|va|vc|ve|vg|vi|vn|vu" +
			"|wf|ws|ye|yt|yu|za|zm|zw";
	
	private static final String GEN_TLDS = "aero|biz|cat|com|coop|edu|gov" +
			"|info|int|jobs|mil|mobi|museum|name|net|org|pro|travel";
	
	
	private static final String ALL_TLD_PATTERN = CC_TLDS + "|" + GEN_TLDS;

	private static final String IP_PATTERN = "[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+";
	
    private static final Pattern AUTHORITY_REGEX =
        Pattern.compile("(([0-9a-z_.-]+)\\.(" + ALL_TLD_PATTERN + "))|" +
        		"(" + IP_PATTERN + ")");

//    private static final Pattern AUTHORITY_REGEX_SIMPLE =
//        Pattern.compile("([0-9a-z_.-]++)");
    private static final Pattern HOST_REGEX_SIMPLE =
        Pattern.compile("(?:[0-9a-z_.:-]+@)?([0-9a-z_.-]++)");
    private static final Pattern USERINFO_REGEX_SIMPLE =
        Pattern.compile("([0-9a-z_.:-]+)(?:@[0-9a-z_.-]++)");

    /**
     * Tests if the String argument looks like it could be a legitimate 
     * authority fragment of a URL, that is, is it an IP address, or, are the
     * characters legal in an authority, and does the string end with a legal
     * TLD.
     * 
	 * @param authString String representation of a fragment of a URL 
	 * @return boolean indicating whether urlPart might be an Authority.
	 */
	public static boolean isAuthority(String authString) {
		Matcher m = AUTHORITY_REGEX.matcher(authString);
		
		return (m != null) && m.matches();
	}
	
	/**
	 * Resolve a possibly relative url argument against a base URL.
	 * @param baseUrl the base URL against which the url should be resolved
	 * @param url the URL, possibly relative, to make absolute.
	 * @return url resolved against baseUrl, unless it is absolute already, and
	 * further transformed by whatever escaping normally takes place with a 
	 * UURI.
	 */
	public static String resolveUrl(String baseUrl, String url) {
		for(final String scheme : ALL_SCHEMES) {
			if(url.startsWith(scheme)) {
				try {
					return UURIFactory.getInstance(url).getEscapedURI();
				} catch (URIException e) {
					LOGGER.warn(e.getLocalizedMessage() + ": " + url);
					// can't let a space exist... send back close to whatever came
					// in...
					return url.replace(" ", "%20");
				}
			}
		}
		UURI absBaseURI;
		UURI resolvedURI = null;
		try {
			absBaseURI = UURIFactory.getInstance(baseUrl);
			resolvedURI = UURIFactory.getInstance(absBaseURI, url);
		} catch (URIException e) {
			LOGGER.warn(e.getLocalizedMessage() + ": " + url);
			return url.replace(" ", "%20");
		}
		return resolvedURI.getEscapedURI();
	}
	
	/**
	 * Attempt to find the scheme (http://, https://, etc) from a given URL.
	 * @param url URL String to parse for a scheme.
	 * @return the scheme, including trailing "://" if known, null otherwise.
	 */
	public static String urlToScheme(final String url) {
		for(final String scheme : ALL_SCHEMES) {
			if(url.startsWith(scheme)) {
				return scheme;
			}
		}
		return null;
	}
	
	/**
	 * Return the default port for the scheme String argument, if known.
	 * @param scheme String scheme, including '://', as in, "http://", "ftp://"
	 * @return the default port for the scheme, or -1 if the scheme isn't known.
	 */
	public static int schemeToDefaultPort(final String scheme) {
		if(scheme.equals(HTTP_SCHEME)) {
			return 80;
		}
		if(scheme.equals(HTTPS_SCHEME)) {
			return 443;
		}
		if(scheme.equals(FTP_SCHEME)) {
			return 21;
		}
		if(scheme.equals(RTSP_SCHEME)) {
			return 554;
		}
		if(scheme.equals(MMS_SCHEME)) {
			return 1755;
		}
		return -1;
	}

	/**
	 * Attempt to extract the path component of a url String argument.
	 * @param url the URL which may contain a path, sans scheme.
	 * @return the path component of the URL, or "" if it contains no path.
	 */
	public static String getURLPath(String url) {
		int portIdx = url.indexOf(UrlOperations.PORT_SEPARATOR);
		int pathIdx = url.indexOf(UrlOperations.PATH_START);
		if(portIdx == -1 && pathIdx == -1) {
			return "";
		}
		if(portIdx == -1) {
			return url.substring(pathIdx);
		}
		if(pathIdx == -1) {
			return url.substring(portIdx);
		}
		if(pathIdx > portIdx) {
			return url.substring(portIdx);
		} else {
			return url.substring(pathIdx);
		}
	}

	/**
	 * Attempt to extract the hostname component of an absolute URL argument.
	 * @param url the url String from which to extract the hostname
	 * @return the hostname within the URL, or the url argument if the host 
	 * cannot be found.
	 */
	public static String urlToHost(String url) {
		String lcUrl = url.toLowerCase();
		if(lcUrl.startsWith("dns:")) {
			return lcUrl.substring(4);
		}
		for(String scheme : ALL_SCHEMES) {
			if(lcUrl.startsWith(scheme)) {
				int authorityIdx = scheme.length();

				Matcher m = 
					HOST_REGEX_SIMPLE.matcher(lcUrl.substring(authorityIdx));
				if(m.find()) {
					return m.group(1);
				}
			}
		}
		return url;
	}

	/**
	 * Extract userinfo from the absolute URL argument, that is, "username@", or
	 * "username:password@" if present.
	 * @param url the URL from which to extract the userinfo
	 * @return the userinfo found, not including the "@", or null if no userinfo
	 * is found
	 */
	public static String urlToUserInfo(String url) {
		String lcUrl = url.toLowerCase();
		if(lcUrl.startsWith("dns:")) {
			return null;
		}
		for(String scheme : ALL_SCHEMES) {
			if(lcUrl.startsWith(scheme)) {
				int authorityIdx = scheme.length();

				Matcher m = 
					USERINFO_REGEX_SIMPLE.matcher(lcUrl.substring(authorityIdx));
				if(m.find()) {
					return m.group(1);
				}
			}
		}
		return null;
	}
	
	/**
	 * Find and return the parent directory of the URL argument
	 * @param url to find the parent directory of
	 * @return parent directory of URL, or null, if either the url argument is
	 * invalid, or if the url is the root of the authority.
	 */
	public static String getUrlParentDir(String url) {
		
		try {
			UURI uri = UURIFactory.getInstance(url);
			String path = uri.getPath();
			if(path.length() > 1) {
				int startIdx = path.length()-1;
				if(path.charAt(path.length()-1) == '/') {
					startIdx--;
				}
				int idx = path.lastIndexOf('/',startIdx);
				if(idx >= 0) {
					uri.setPath(path.substring(0,idx+1));
					uri.setQuery(null);
					return uri.toString();
				}
			}
		} catch (URIException e) {
			LOGGER.warn(e.getLocalizedMessage() + ": " + url);
		}
		return null;
	}
}
