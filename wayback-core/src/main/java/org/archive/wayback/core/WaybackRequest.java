/* WMRequest
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.requestparser.OpenSearchRequestParser;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Abstraction of all the data associated with a users request to the Wayback
 * Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackRequest {

	public final static String REQUEST_ANCHOR_DATE = "request.anchordate";
	public final static String REQUEST_ANCHOR_WINDOW = "request.anchorwindow";

	private int resultsPerPage = 10;

	private int pageNum = 1;
	
	private String contextPrefix = null;
	private String serverPrefix = null;
	private AccessPoint context = null;
	private ObjectFilter<CaptureSearchResult> exclusionFilter = null;

	private HashMap<String,String> filters = new HashMap<String,String>();
	
	private StringFormatter formatter = null;
	/**
	 * Request: Authorization Type: "BASIC", "SSL", or "" if none.
	 */
	public static final String REQUEST_AUTH_TYPE = "requestauthtype";
	/**
	 * Request: Wayback Context: the string context used in the request,
	 * if applicable.
	 */
	public static final String REQUEST_WAYBACK_CONTEXT = "waybackcontext";
	/**
	 * Request: Wayback Port: the port the remote user connected to for this
	 * request.  
	 */
	public static final String REQUEST_WAYBACK_PORT = "waybackport";
	/**
	 * Request: Wayback Hostname: the string "Host:" HTTP header  
	 */
	public static final String REQUEST_WAYBACK_HOSTNAME = "waybackhostname";
	/**
	 * Request: Remote Address, string IP address: "127.0.0.1" 
	 */
	public static final String REQUEST_REMOTE_ADDRESS = "remoteaddress";
	/**
	 * Request: auto resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_AUTO = "auto";
	/**
	 * Request: year resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_YEARS = "years";
	/**
	 * Request: two-month resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_TWO_MONTHS = "twomonths";
	/**
	 * Request: month resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_MONTHS = "months";
	/**
	 * Request: day resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_DAYS = "days";
	/**
	 * Request: hour resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_HOURS = "hours";
	/**
	 * Request: replay actual document or metadata for document: "yes" means 
	 * replay metadata only, not the actual document: (TimeLine mode)
	 */
	public static final String REQUEST_META_MODE = "metamode";
	/**
	 * Request: resolution of results to be displayed: (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION = "resolution";
	/**
	 * Request: closest type request 
	 */
	public static final String REQUEST_CLOSEST_QUERY = "urlclosestquery";
	/**
	 * Request: replay type request 
	 */
	public static final String REQUEST_REPLAY_QUERY = "replay";
	/**
	 * Request: urlprefixquery type request 
	 */
	public static final String REQUEST_URL_PREFIX_QUERY = "urlprefixquery";
	/**
	 * Request: urlquery type request 
	 */
	public static final String REQUEST_URL_QUERY = "urlquery";
	/**
	 * Request: xml data requested 
	 */
	public static final String REQUEST_XML_DATA = "xmldata";
	/**
	 * Request: defines type - urlquery, urlprefixquery, or replay 
	 */
	public static final String REQUEST_TYPE = "type";
	/**
	 * Request: URL of referrer, if supplied, or "" if not 
	 */
	public static final String REQUEST_REFERER_URL = "refererurl";
	/**
	 * Request: Original URL or URL prefix requested.
	 * This version differs from @{link {@link REQUEST_URL} in that its
	 * the URL before it was passed via the UURIFactory cleanup.
	 */
	public static final String REQUEST_URL_CLEANED = "cleanedurl";
	/**
	 * Request: URL or URL prefix requested 
	 */
	public static final String REQUEST_URL = "url";
	/**
	 * Request: (replay) find closest result to this 14-digit timestamp 
	 */
	public static final String REQUEST_EXACT_DATE = "exactdate";
	/**
	 * Request: filter results after this 14-digit timestamp 
	 */
	public static final String REQUEST_END_DATE = "enddate";
	/**
	 * Request: filter results before this 14-digit timestamp 
	 */
	public static final String REQUEST_START_DATE = "startdate";
	/**
	 * Request: (query) filter results to those prefixed with this (possibly 
	 * partial) 14-digit timestamp 
	 */
	public static final String REQUEST_DATE = "date";
	/**
	 * Request: Remote User or "" if the request did not contain auth info.
	 */
	public static final String REQUEST_REMOTE_USER = "requestremoteuser";
	/**
	 * Request: Best Guess at users requested locale.
	 */
	public static final String REQUEST_LOCALE_LANG = "requestlocalelang";
	/**
	 * Request: Indicates user only wants results that exactly match the 
	 * requested hostname -- no canonicalization.
	 */
	public static final String REQUEST_EXACT_HOST_ONLY = "requestexacthost";
	/**
	 * Request: indicates positive value for any request boolean flag.
	 */
	public static final String REQUEST_YES = "yes";
	private static String UI_RESOURCE_BUNDLE_NAME = "WaybackUI";

	private final static String standardHeaders[] = {
			WaybackRequest.REQUEST_REFERER_URL,
			WaybackRequest.REQUEST_REMOTE_ADDRESS,
			WaybackRequest.REQUEST_WAYBACK_HOSTNAME,
			WaybackRequest.REQUEST_WAYBACK_PORT,
			WaybackRequest.REQUEST_WAYBACK_CONTEXT,
			WaybackRequest.REQUEST_AUTH_TYPE,
			WaybackRequest.REQUEST_REMOTE_USER,
			WaybackRequest.REQUEST_LOCALE_LANG };

	/**
	 * Constructor, possibly/probably this should BE a Properties, instead of
	 * HAVEing a Properties...
	 */
	public WaybackRequest() {
		super();
	}

	/**
	 * @return true if REQUEST_TYPE is set, and is set to REQUEST_REPLAY_QUERY
	 */
	public boolean isReplayRequest() {
		String type = get(WaybackRequest.REQUEST_TYPE);
		if(type != null && type.equals(WaybackRequest.REQUEST_REPLAY_QUERY)) {
			return true;
		}
		return false;
	}
	/**
	 * @return true if true if REQUEST_TYPE is not set, or is set to a value 
	 * other than REQUEST_REPLAY_QUERY
	 */
	public boolean isQueryRequest() {
		return !isReplayRequest();
	}

	/**
	 * @return Returns the pageNum.
	 */
	public int getPageNum() {
		return pageNum;
	}

	/**
	 * @param pageNum
	 *            The pageNum to set.
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/**
	 * @return Returns the resultsPerPage.
	 */
	public int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * @param resultsPerPage
	 *            The resultsPerPage to set.
	 */
	public void setResultsPerPage(int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	/**
	 * @param key
	 * @return boolean, true if the request contains key 'key'
	 */
	public boolean containsKey(String key) {
		return filters.containsKey(key);
	}

	/**
	 * @param key
	 * @return String value for key 'key', or null if no value exists
	 */
	public String get(String key) {
		return (String) filters.get(key);
	}

	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		filters.put(key, value);
	}

	private String emptyIfNull(String arg) {
		if (arg == null) {
			return "";
		}
		return arg;
	}
	
	/**
	 * Set the Locale for the request, which impacts UI Strings
	 * @param l
	 */
	public void setLocale(Locale l) {
		ResourceBundle b = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME,l);
		formatter = new StringFormatter(b,l);
	}
	
	private String getUserLocale(HttpServletRequest httpRequest) {
		Locale l = httpRequest.getLocale();
		ResourceBundle b = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME,
				httpRequest.getLocale());
		formatter = new StringFormatter(b,l);
		return emptyIfNull(httpRequest.getLocale().getDisplayLanguage());
	}

	/**
	 * extract REFERER, remote IP and authorization information from the
	 * HttpServletRequest
	 * 
	 * @param httpRequest
	 */
	private void extractHttpRequestInfo(HttpServletRequest httpRequest) {
		// attempt to get the HTTP referer if present..
		put(WaybackRequest.REQUEST_REFERER_URL, emptyIfNull(httpRequest
				.getHeader("REFERER")));
		put(WaybackRequest.REQUEST_REMOTE_ADDRESS, emptyIfNull(httpRequest
				.getRemoteAddr()));
		put(WaybackRequest.REQUEST_WAYBACK_HOSTNAME, emptyIfNull(httpRequest
				.getLocalName()));
		put(WaybackRequest.REQUEST_WAYBACK_PORT, String.valueOf(httpRequest
				.getLocalPort()));
		put(WaybackRequest.REQUEST_WAYBACK_CONTEXT, emptyIfNull(httpRequest
				.getContextPath()));
		put(WaybackRequest.REQUEST_AUTH_TYPE, emptyIfNull(httpRequest
				.getAuthType()));
		put(WaybackRequest.REQUEST_REMOTE_USER, emptyIfNull(httpRequest
				.getRemoteUser()));
		put(WaybackRequest.REQUEST_LOCALE_LANG,getUserLocale(httpRequest));

		Cookie[] cookies = httpRequest.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				put(cookie.getName(),cookie.getValue());
			}
		}
	}

	/**
	 * @param prefix
	 */
	public void setServerPrefix(String prefix) {
		serverPrefix = prefix;
	}

	/**
	 * @param prefix
	 * @return an absolute String URL that will point to the root of the
	 * server that is handling the request. 
	 */
	public String getServerPrefix() {
		if(serverPrefix == null) {
			return "";
		}
		return serverPrefix;
	}

	
	/**
	 * @param prefix
	 */
	public void setContextPrefix(String prefix) {
		contextPrefix = prefix;
	}
	/**
	 * Construct an absolute URL that points to the root of the context that
	 * recieved the request, including a trailing "/".
	 * 
	 * @return String absolute URL pointing to the Context root where the
	 *         request was revieved.
	 */
	public String getContextPrefix() {
		if(contextPrefix == null) {
			return "";
		}
		return contextPrefix;
	}

	/**
	 * attempt to fixup this WaybackRequest, mostly with respect to dates: if
	 * only "date" was specified, infer start and end dates from it. Also grab
	 * useful info from the HttpServletRequest, cookies, remote address, etc.
	 * 
	 * @param httpRequest
	 */
	public void fixup(HttpServletRequest httpRequest) {
		extractHttpRequestInfo(httpRequest);
		String startDate = get(WaybackRequest.REQUEST_START_DATE);
		String endDate = get(WaybackRequest.REQUEST_END_DATE);
		String exactDate = get(WaybackRequest.REQUEST_EXACT_DATE);
		String partialDate = get(WaybackRequest.REQUEST_DATE);
		if (partialDate == null) {
			partialDate = "";
		}
		if (startDate == null || startDate.length() == 0) {
			put(WaybackRequest.REQUEST_START_DATE, Timestamp
					.padStartDateStr(partialDate));
		} else if (startDate.length() < 14) {
			put(WaybackRequest.REQUEST_START_DATE, Timestamp
					.padStartDateStr(startDate));
		}
		if (endDate == null || endDate.length() == 0) {
			put(WaybackRequest.REQUEST_END_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (endDate.length() < 14) {
			put(WaybackRequest.REQUEST_END_DATE, Timestamp
					.padEndDateStr(endDate));
		}
		if (exactDate == null || exactDate.length() == 0) {
			put(WaybackRequest.REQUEST_EXACT_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (exactDate.length() < 14) {
			put(WaybackRequest.REQUEST_EXACT_DATE, Timestamp
					.padEndDateStr(exactDate));
		}
	}

	/**
	 * @return String hex-encoded GET CGI arguments which will duplicate this
	 *         wayback request
	 */
	public String getQueryArguments() {
		return getQueryArguments(pageNum);
	}

	/**
	 * @param pageNum
	 * @return String hex-encoded GET CGI arguments which will duplicate the
	 *         same request, but for page 'pageNum' of the results
	 */
	public String getQueryArguments(int pageNum) {
		int numPerPage = resultsPerPage;

		StringBuffer queryString = new StringBuffer("");
		Iterator<String> itr = filters.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			boolean isStandard = false;
			for(int i=0; i<standardHeaders.length; i++) {
				if(standardHeaders[i].equals(key)) {
					isStandard = true;
					break;
				}
			}
			if(isStandard) continue;
			String val = filters.get(key);
			if (queryString.length() > 0) {
				queryString.append(" ");
			}
			queryString.append(key + ":" + val);
		}
		String escapedQuery = queryString.toString();

		try {

			escapedQuery = URLEncoder.encode(escapedQuery, "UTF-8");

		} catch (UnsupportedEncodingException e) {
			// oops.. what to do?
			e.printStackTrace();
		}
		return OpenSearchRequestParser.SEARCH_QUERY + "=" + escapedQuery + "&"
				+ OpenSearchRequestParser.SEARCH_RESULTS + "=" + numPerPage + "&"
				+ OpenSearchRequestParser.START_PAGE + "=" + pageNum;
	}

    /**
     * Set the request URL.
     * Also populates request url cleaned.
     * @param urlStr Request URL.
     * @throws URIException
     */
	public void setRequestUrl(String urlStr) throws URIException {
	    if (!urlStr.startsWith("http://")) {
	    	if(urlStr.startsWith("http:/")) {
	    		urlStr = "http://" + urlStr.substring(6);
	    	} else {
	    		urlStr = "http://" + urlStr;
	    	}
	    }
        // If its not http, next line throws exception. TODO: Fix.
	    UURI requestURI = UURIFactory.getInstance(urlStr);
	    put(WaybackRequest.REQUEST_URL_CLEANED, requestURI.toString());
        put(WaybackRequest.REQUEST_URL, urlStr);
	}

	/**
	 * @return StringFormatter based on user request info
	 */
	public StringFormatter getFormatter() {
		if(formatter == null) {
			setLocale(Locale.getAvailableLocales()[0]);
		}
		return formatter;
	}
	public WaybackRequest clone() {
		WaybackRequest wbRequest = new WaybackRequest();

		wbRequest.resultsPerPage = resultsPerPage;

		wbRequest.pageNum = pageNum;
		
		wbRequest.contextPrefix = contextPrefix;
		wbRequest.serverPrefix = serverPrefix;

		wbRequest.formatter = formatter;

		wbRequest.filters = new HashMap<String,String>();
		Iterator<String> itr = filters.keySet().iterator();
		while(itr.hasNext()) {
			String key = itr.next();
			String val = filters.get(key);
			wbRequest.filters.put(key, val);
		}
		return wbRequest;
	}

	/**
	 * @return the context
	 */
	public AccessPoint getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(AccessPoint context) {
		this.context = context;
	}

	public ObjectFilter<CaptureSearchResult> getExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(ObjectFilter<CaptureSearchResult> exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}

	public Set<String> keySet() {
		return filters.keySet();
	}
}