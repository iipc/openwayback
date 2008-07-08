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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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

	/**
	 * indicates the number of requests per page, only makes sense for 
	 * Capture/Url queries.
	 */
	private int resultsPerPage = 10;
	/**
	 * indicates the specific page of results to show, for paginated requests, 
	 * only makes sense for Capture/Url queries.
	 */
	private int pageNum = 1;
	/**
	 * absolute URL prefix to the AccessPoint which received this request
	 */
	private String contextPrefix = null;
	/**
	 * absolute URL prefix to the Server(webapp) which received this request 
	 */
	private String serverPrefix = null;
	/**
	 * reference to the AccessPoint which received this request.
	 */
	private AccessPoint accessPoint = null;
	/**
	 * custom CaptureSearchResult Filter to use for this specific request. Can
	 * be null, and is sometimes useful to allow an AccessPoint to have specific
	 * and possibly variable filters.
	 */
	private ObjectFilter<CaptureSearchResult> exclusionFilter = null;
	/**
	 * StringFormatter object set up with the users specific Locale, and the
	 * Wayback UI ResourceBundle prepared for use, simplifying UI generation
	 * somewhat.
	 */
	private StringFormatter formatter = null;
	/**
	 * generic String-to-String map of various request filters and type 
	 * information. See constants below for keys & values.
	 */
	private HashMap<String,String> filters = new HashMap<String,String>();
	

	/*
	 * **********************
	 * REQUEST TYPE CONSTANTS 
	 * **********************
	 */
	/**
	 * specifies the TYPE of the this particular request. One of:
	 * *) REQUEST_REPLAY_QUERY
	 * *) REQUEST_CAPTURE_QUERY
	 * *) REQUEST_URL_QUERY
	 */
	public static final String REQUEST_TYPE = "type";
	/**
	 * REQUEST_TYPE option indicating a request for Replay of the Resource
	 * matching REQUEST_URL closest in time to REQUEST_DATE 
	 */
	public static final String REQUEST_REPLAY_QUERY = "replay";
	/**
	 * REQUEST_TYPE option indicating a query against the ResourceIndex for 
	 * captures of URLs matching the REQUEST_URL 
	 */
	public static final String REQUEST_CAPTURE_QUERY = "capturequery";
	/**
	 * REQUEST_TYPE option indicating a query against the ResourceIndex for 
	 * summaries of URLs prefixed with the REQUEST_URL 
	 */
	public static final String REQUEST_URL_QUERY = "urlquery";
	/*
	 * **********************
	 * /REQUEST TYPE CONSTANTS 
	 * **********************
	 */
	

	/*
	 * ******************
	 * URL/DATE CONSTANTS 
	 * ******************
	 */
	/**
	 * GUARANTEED PRESENT: Original(RAW) URL or URL prefix requested, before any 
	 * cleanup/fixing 
	 */
	public static final String REQUEST_URL = "url";

	/**
	 * Cleaned up version of original requested URL or URL prefix, as performed
	 * by UURIFactory.
	 */
//	public static final String REQUEST_URL_CLEANED = "cleanedurl";

	/**
	 * GUARANTEED PRESENT: omit results after this 14-digit String timestamp.
	 * Possibly created from:
	 * 1) specified directly in request
	 * 2) a partial REQUEST_DATE (latest possible given a prefix)
	 * 3) RequestParser default
	 * 4) 14-digit representation of the moment the request was recieved.
	 */
	public static final String REQUEST_END_DATE = "enddate";

	/**
	 * GUARANTEED PRESENT: omit results before this 14-digit String timestamp.
	 * Possibly created from:
	 * 1) specified directly in request
	 * 2) a partial REQUEST_DATE (earliest possible given a prefix)
	 * 3) RequestParser default
	 * 4) 14-digit representation of midnight Jan 1, 1996.
	 */
	public static final String REQUEST_START_DATE = "startdate";

	/**
	 * GUARANTEED PRESENT for Replay requests only. If present for Query 
	 * requests, then it will be interpreted as a partial timestamp for missing
	 * REQUEST_START_DATE and REQUEST_END_DATE fields.
	 * Original (RAW/possibly partial) 14-digit timestamp of date requested for
	 * Replay 
	 */
	public static final String REQUEST_DATE = "date";

	/**
	 * GUARANTEED PRESENT for Replay requests only, no meaning for Query 
	 * requests.
	 * Cleaned up version of original REQUEST_DATE, padded to 14 digits assuming
	 * the 
	 */
	public static final String REQUEST_EXACT_DATE = "exactdate";

	/**
	 * Indicates user only wants results that exactly match the hostname within 
	 * REQUEST_URL -- no canonicalization.
	 */
	public static final String REQUEST_EXACT_HOST_ONLY = "requestexacthost";

	/**
	 * indicates positive value for any request boolean flag.
	 */
	public static final String REQUEST_YES = "yes";

	/**
	 * Replay-Only: indicates the date to tend towards when computing closest
	 * matches within time. Used to prevent "time drift" while surfing from a 
	 * particular date.
	 */
	public final static String REQUEST_ANCHOR_DATE = "request.anchordate";

	/**
	 * Replay-Only: String representation of number of seconds. Used only in 
	 * conjunction with REQUEST_ANCHOR_DATE, and indicates that documents more
	 * than this many seconds should not be shown in a replay session. Useful 
	 * for QA purposes, to ensure that all content within a replay session was
	 * crawled near a particular point, the REQUEST_ANCHOR_DATE.
	 */
	public final static String REQUEST_ANCHOR_WINDOW = "request.anchorwindow";
	/*
	 * ******************
	 * /URL/DATE CONSTANTS 
	 * ******************
	 */


	/*
	 * *******************************
	 * OUTPUT TYPE CONSTANTS 
	 * *******************************
	 */
	/**
	 * Request: replay actual document or metadata for document: "yes" means 
	 * replay metadata only, not the actual document: (TimeLine mode)
	 */
	public static final String REQUEST_META_MODE = "metamode";
	/**
	 * Request: xml data requested 
	 */
	public static final String REQUEST_XML_DATA = "xmldata";
	/*
	 * *******************************
	 * /OUTPUT TYPE CONSTANTS 
	 * *******************************
	 */

	/*
	 * *******************************
	 * CONTEXT & ACCESSPOINT CONSTANTS 
	 * *******************************
	 */
	/**
	 * the string (webapp) context that received this request
	 */
	public static final String REQUEST_WAYBACK_CONTEXT = "waybackcontext";
	/**
	 * the port the remote user connected to for this request  
	 */
	public static final String REQUEST_WAYBACK_PORT = "waybackport";
	/*
	 * *******************************
	 * /CONTEXT & ACCESSPOINT CONSTANTS 
	 * *******************************
	 */

	/*
	 * *****************************
	 * HTTP HEADER/REQUEST CONSTANTS 
	 * *****************************
	 */
	/**
	 * incoming requests HTTP "Host:" header, or null
	 */
	public static final String REQUEST_WAYBACK_HOSTNAME = "waybackhostname";
	/**
	 * incoming requests HTTP "Referer:" header, or null 
	 */
	public static final String REQUEST_REFERER_URL = "refererurl";
	/**
	 * Remote Address that connected to this webapp to create the request
	 * string IP address: "127.0.0.1" 
	 */
	public static final String REQUEST_REMOTE_ADDRESS = "remoteaddress";
	/**
	 * Remote User or null if the request did not contain auth info.
	 * see HttpServletRequest.getRemoteUser()
	 */
	public static final String REQUEST_REMOTE_USER = "requestremoteuser";

	/**
	 * User Locale name: Best Guess at users requested locale.
	 * see ServletRequest.getLocale().getDisplayLanguage()
	 */
	public static final String REQUEST_LOCALE_LANG = "requestlocalelang";
	/**
	 * Authorization Type: "BASIC", "SSL", or null if none. 
	 * see HttpServletRequest.getAuthType()
	 */
	public static final String REQUEST_AUTH_TYPE = "requestauthtype";
	/*
	 * ***********************
	 * /HTTP HEADER/REQUEST CONSTANTS 
	 * ***********************
	 */
	
	/*
	 * ***********************
	 * TIMELINE MODE CONSTANTS 
	 * ***********************
	 */
	/**
	 * resolution of results to be displayed: (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION = "resolution";
	/**
	 * auto resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_AUTO = "auto";
	/**
	 * year resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_YEARS = "years";
	/**
	 * two-month resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_TWO_MONTHS = "twomonths";
	/**
	 * month resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_MONTHS = "months";
	/**
	 * day resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_DAYS = "days";
	/**
	 * hour resolution (TimeLine mode)
	 */
	public static final String REQUEST_RESOLUTION_HOURS = "hours";
	/*
	 * ***********************
	 * /TIMELINE MODE CONSTANTS 
	 * ***********************
	 */
	
	private static String UI_RESOURCE_BUNDLE_NAME = "WaybackUI";

	/**
	 * set of filter keys that are not forwarded to subsequent paginated 
	 * requests.
	 */
	private final static String standardHeaders[] = {
			REQUEST_REFERER_URL,
			REQUEST_REMOTE_ADDRESS,
			REQUEST_WAYBACK_HOSTNAME,
			REQUEST_WAYBACK_PORT,
			REQUEST_WAYBACK_CONTEXT,
			REQUEST_AUTH_TYPE,
			REQUEST_REMOTE_USER,
			REQUEST_LOCALE_LANG };

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
	 * @param prefix
	 */
	public void setContextPrefix(String prefix) {
		contextPrefix = prefix;
	}

	/**
	 * Construct an absolute URL that points to the root of the context that
	 * received the request, including a trailing "/".
	 * 
	 * @return String absolute URL pointing to the Context root where the
	 *         request was received.
	 */
	public String getContextPrefix() {
		if(contextPrefix == null) {
			return "";
		}
		return contextPrefix;
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
	 * @return the accessPoint
	 */
	public AccessPoint getAccessPoint() {
		return accessPoint;
	}

	/**
	 * @param accessPoint the accessPoint to set
	 */
	public void setAccessPoint(AccessPoint accessPoint) {
		this.accessPoint = accessPoint;
	}

	public ObjectFilter<CaptureSearchResult> getExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(ObjectFilter<CaptureSearchResult> exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
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

	/**
	 * @param key
	 * @return String value for key 'key', or null if no value exists
	 */
	public String get(String key) {
		return filters.get(key);
	}

	/**
	 * @param key
	 * @param value
	 */
	public void put(String key, String value) {
		filters.put(key, value);
	}
	public void remove(String key) {
		filters.remove(key);
	}

	private void setBoolean(String key, boolean value) {
		if(value) {
			put(key,REQUEST_YES);
		} else {
			remove(key);
		}
	}	
	private boolean getBoolean(String key) {
		String value = get(key);
		return(value != null && value.equals(REQUEST_YES));
	}	
	/**
	 * @param key
	 * @return boolean, true if the request contains key 'key'
	 * @deprecated
	 */
	public boolean containsKey(String key) {
		return filters.containsKey(key);
	}

	private void putUnlessNull(String key, String val) {
		if (val != null) {
			put(key,val);
		}
	}

	private boolean isRequestType(String requestType) {
		String type = get(REQUEST_TYPE);
		if(type != null && type.equals(requestType)) {
			return true;
		}
		return false;
	}

	/**
	 * @return true if this is a Replay request
	 */
	public boolean isReplayRequest() {
		return isRequestType(REQUEST_REPLAY_QUERY);
	}
	/**
	 * marks this request as a Replay request
	 */
	public void setReplayRequest() {
		put(REQUEST_TYPE,REQUEST_REPLAY_QUERY);
	}
	/**
	 * @return true if this is a Capture Query request
	 */
	public boolean isCaptureQueryRequest() {
		return isRequestType(REQUEST_CAPTURE_QUERY);
	}
	/**
	 * marks this request as a Replay request
	 */
	public void setCaptureQueryRequest() {
		put(REQUEST_TYPE,REQUEST_CAPTURE_QUERY);
	}
	/**
	 * @return true if this is an Url Query request
	 */
	public boolean isUrlQueryRequest() {
		return isRequestType(REQUEST_URL_QUERY);
	}
	/**
	 * marks this request as a Replay request
	 */
	public void setUrlQueryRequest() {
		put(REQUEST_TYPE,REQUEST_URL_QUERY);
	}

	public String getRequestUrl() {
		return get(REQUEST_URL);
	}
    /**
     * Set the request URL.
     * @param urlStr Request URL.
     */
	public void setRequestUrl(String urlStr) {
		// TODO: fix this to use other schemes
		if (!urlStr.startsWith("http://")) {
	    	if(urlStr.startsWith("http:/")) {
	    		urlStr = "http://" + urlStr.substring(6);
	    	} else {
	    		urlStr = "http://" + urlStr;
	    	}
	    }
//	    UURI requestURI = UURIFactory.getInstance(urlStr);
//	    put(REQUEST_URL_CLEANED, requestURI.toString());
        put(REQUEST_URL, urlStr);
	}
	
	public String getEndTimestamp() {
		return get(REQUEST_END_DATE);
	}
	public Date getEndDate() {
		return Timestamp.parseAfter(get(REQUEST_END_DATE)).getDate();
	}
	public void setEndDate(Date date) {
		put(REQUEST_END_DATE,new Timestamp(date).getDateStr());
	}
	public void setEndTimestamp(String timestamp) {
		put(REQUEST_END_DATE,timestamp);
	}

	public String getStartTimestamp() {
		return get(REQUEST_START_DATE);
	}
	public Date getStartDate() {
		return Timestamp.parseBefore(get(REQUEST_START_DATE)).getDate();
	}
	public void setStartDate(Date date) {
		put(REQUEST_START_DATE,new Timestamp(date).getDateStr());
	}
	public void setStartTimestamp(String timestamp) {
		put(REQUEST_START_DATE,timestamp);
	}
	
	public String getReplayTimestamp() {
		return get(REQUEST_DATE);
	}
	public Date getReplayDate() {
		return Timestamp.parseAfter(get(REQUEST_DATE)).getDate();
	}
	public void setReplayDate(Date date) {
		put(REQUEST_DATE,new Timestamp(date).getDateStr());
	}
	public void setReplayTimestamp(String timestamp) {
		put(REQUEST_DATE,timestamp);
	}

	public void setExactHost(boolean isExactHost) {
		setBoolean(REQUEST_EXACT_HOST_ONLY,isExactHost);
	}
	public boolean isExactHost() {
		return getBoolean(REQUEST_EXACT_HOST_ONLY);
	}
	
	public String getAnchorTimestamp() {
		return get(REQUEST_ANCHOR_DATE);
	}
	public Date getAnchorDate() {
		return Timestamp.parseAfter(get(REQUEST_ANCHOR_DATE)).getDate();
	}
	public void setAnchorDate(Date date) {
		put(REQUEST_ANCHOR_DATE,new Timestamp(date).getDateStr());
	}
	public void setAnchorTimestamp(String timestamp) {
		put(REQUEST_ANCHOR_DATE,timestamp);
	}

	public long getAnchorWindow() {
		String seconds = get(REQUEST_ANCHOR_WINDOW);
		if(seconds == null) {
			return 0;
		}
		return Long.parseLong(seconds);
	}
	public void setAnchorWindow(long seconds) {
		put(REQUEST_ANCHOR_WINDOW,String.valueOf(seconds));;
	}

	public void setMetaMode(boolean isMetaMode) {
		setBoolean(REQUEST_META_MODE,isMetaMode);
	}
	public boolean isMetaMode() {
		return getBoolean(REQUEST_META_MODE);
	}

	public void setXMLMode(boolean isXMLMode) {
		setBoolean(REQUEST_XML_DATA,isXMLMode);
	}
	public boolean isXMLMode() {
		return getBoolean(REQUEST_XML_DATA);
	}
	
	public String getWaybackContext() {
		return get(REQUEST_WAYBACK_CONTEXT);
	}
	public int getWaybackPort() {
		String port = get(REQUEST_WAYBACK_PORT);
		if(port == null) {
			return 0;
		}
		return Integer.parseInt(port);
	}
	
	public String getWaybackHostname() {
		return get(REQUEST_WAYBACK_HOSTNAME);
	}
	public String getRefererUrl() {
		return get(REQUEST_REFERER_URL);
	}
	public String getRemoteIPAddress() {
		return get(REQUEST_REMOTE_ADDRESS);
	}
	public String getRemoteUser() {
		return get(REQUEST_REMOTE_USER);
	}
	public String getLocaleLanguage() {
		return get(REQUEST_LOCALE_LANG);
	}
	public String getAuthType() {
		return get(REQUEST_AUTH_TYPE);
	}
	
	public String getTimelineResolution() {
		return get(REQUEST_RESOLUTION);
	}
	public void setTimelineAutoResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_AUTO);
	}
	public void setTimelineYearResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_YEARS);
	}
	public void setTimelineTwoMonthResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_TWO_MONTHS);
	}
	public void setTimelineMonthResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_MONTHS);
	}
	public void setTimelineDayResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_DAYS);
	}
	public void setTimelineHourResolution() {
		put(REQUEST_RESOLUTION,REQUEST_RESOLUTION_HOURS);
	}
	
	/**
	 * Set the Locale for the request, which impacts UI Strings
	 * @param l
	 */
	public void setLocale(Locale l) {
		ResourceBundle b = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME,l);
		formatter = new StringFormatter(b,l);
	}
	
	/**
	 * extract REFERER, remote IP and authorization information from the
	 * HttpServletRequest
	 * 
	 * @param httpRequest
	 */
	private void extractHttpRequestInfo(HttpServletRequest httpRequest) {
		
		putUnlessNull(REQUEST_REFERER_URL, httpRequest.getHeader("REFERER"));
		putUnlessNull(REQUEST_REMOTE_ADDRESS, httpRequest.getRemoteAddr());
		putUnlessNull(REQUEST_WAYBACK_HOSTNAME, httpRequest.getLocalName());
		putUnlessNull(REQUEST_AUTH_TYPE, httpRequest.getAuthType());
		putUnlessNull(REQUEST_REMOTE_USER, httpRequest.getRemoteUser());
		putUnlessNull(REQUEST_WAYBACK_PORT, 
				String.valueOf(httpRequest.getLocalPort()));
		putUnlessNull(REQUEST_WAYBACK_CONTEXT, httpRequest.getContextPath());

		Locale l = httpRequest.getLocale();
		ResourceBundle b = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME,
				httpRequest.getLocale());
		formatter = new StringFormatter(b,l);
		putUnlessNull(REQUEST_LOCALE_LANG,l.getDisplayLanguage());

		Cookie[] cookies = httpRequest.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				put(cookie.getName(),cookie.getValue());
			}
		}
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
		String startDate = get(REQUEST_START_DATE);
		String endDate = get(REQUEST_END_DATE);
		String exactDate = get(REQUEST_EXACT_DATE);
		String partialDate = get(REQUEST_DATE);
		if (partialDate == null) {
			partialDate = "";
		}
		if (startDate == null || startDate.length() == 0) {
			put(REQUEST_START_DATE, Timestamp
					.padStartDateStr(partialDate));
		} else if (startDate.length() < 14) {
			put(REQUEST_START_DATE, Timestamp
					.padStartDateStr(startDate));
		}
		if (endDate == null || endDate.length() == 0) {
			put(REQUEST_END_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (endDate.length() < 14) {
			put(REQUEST_END_DATE, Timestamp
					.padEndDateStr(endDate));
		}
		if (exactDate == null || exactDate.length() == 0) {
			put(REQUEST_EXACT_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (exactDate.length() < 14) {
			put(REQUEST_EXACT_DATE, Timestamp
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
	 * 
	 * @return
	 * @deprecated
	 */
	public Set<String> keySet() {
		return filters.keySet();
	}
}