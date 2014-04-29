/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.requestparser.OpenSearchRequestParser;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
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
	 * and possibly variable filters. These filters relate specifically to
	 * exclusion of results from the ResourceIndex. Compared to the 
	 * resultFilters, if these filters redact all results, then an 
	 * AccessControlException will be thrown.
	 */
	private ExclusionFilter exclusionFilter = null;

	/**
	 * custom CaptureSearchResult Filter to use for this specific request. Can
	 * be null, and is sometimes useful to allow an AccessPoint to have specific
	 * and possibly variable filters.
	 */
	private ObjectFilterChain<CaptureSearchResult> resultFilters = null;
	
	
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
	public static final String REQUEST_CAPTURE_QUERY = "urlquery";
	/**
	 * REQUEST_TYPE option indicating a query against the ResourceIndex for 
	 * summaries of URLs prefixed with the REQUEST_URL 
	 */
	public static final String REQUEST_URL_QUERY = "prefixquery";
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
	 * Indicates user only wants results that were captured using the same 
	 * scheme as that specified in REQUEST_URL.
	 */
	public static final String REQUEST_EXACT_SCHEME_ONLY = "requestexactscheme";

	/**
	 * Indicates user requested content from proxied from the live web.
	 */
	public static final String REQUEST_IS_LIVE_WEB = "requestliveweb";
	
	/**
	 * indicates positive value for any request boolean flag.
	 */
	public static final String REQUEST_YES = "yes";
	
	/**
	 * indicates request for latest 'best' capture
	 * This can be used to provide 'best' latest replay, skipping error captures, redirects
	 * 
	 */
	public static final String REQUEST_LATEST_BEST_REPLAY = "latestbestreplay";

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
	/**
	 * Request: CSS context requested 
	 */
	public static final String REQUEST_CSS_CONTEXT = "csscontext";
	/**
	 * Request: JS context requested 
	 */
	public static final String REQUEST_JS_CONTEXT = "jscontext";
	/**
	 * Request: IMG context requested 
	 */
	public static final String REQUEST_IMAGE_CONTEXT = "imagecontext";
	/**
	 * Request: OBJECT or EMBED context requested 
	 */
	public static final String REQUEST_OBJECT_EMBED_CONTEXT = "objectembedcontext";

	/**
	 * Request: Identity context requested (totally transparent) 
	 */
	public static final String REQUEST_IDENTITY_CONTEXT = "identitycontext";
		
	/**
	 * Request: Content should be wrapped in a frame 
	 */
	public static final String REQUEST_FRAME_WRAPPER_CONTEXT = 
		"framewrappercontext";
		
	/**
	 * Request: Display context for embedded metadata in an IFrame
	 */
	public static final String REQUEST_IFRAME_WRAPPER_CONTEXT = 
		"iframewrappercontext";
	
	/**
	 * Request: Ajax request -- don't insert extra headers and footers
	 */
	public static final String REQUEST_AJAX_REQUEST = "requestajaxrequest";
	
	/**
	 * Request: Memento Accept-Datetime used -- don't add extra redirects
	 */
	public static final String REQUEST_MEMENTO_ACCEPT_DATETIME = "requestmementoacceptdatetime";	
	
	/**
	 * Request: Memento Timemap Request
	 */
	public static final String REQUEST_MEMENTO_TIMEMAP = "requestmementotimemap";
	
	/**
	 * Request: Memento Timegate Request
	 */
	public static final String REQUEST_MEMENTO_TIMEGATE = "requestmementotimegate";
	
	/**
	 * Request: Charset detection mode 
	 */
	public static final String REQUEST_CHARSET_MODE = "charsetmode";
	
	
	/**
	 * Request: Use timestamp as part of the search key
	 */
	public static final String REQUEST_TIMESTAMP_SEARCH_KEY = "timestampsearchkey";
	
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
	 * Exact value from HTTP request for header "Authorization"
	 */
	public static final String REQUEST_AUTHORIZATION = "Authorization";

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

	private static String STD_LOGGED_IN_VER = "logged-in-ver";
	private static String STD_LOGGED_IN_NAME = "logged-in-name";
	private static String STD_LOGGED_IN_USER = "logged-in-user";
	private static String STD_PHP_SESSION_ID = "PHPSESSID";
	private static String STD_J_SESSION_ID = "JSESSIONID";
	
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
			REQUEST_LOCALE_LANG,
			REQUEST_AJAX_REQUEST,
			STD_LOGGED_IN_USER,
			STD_LOGGED_IN_VER,
			STD_LOGGED_IN_NAME,
			STD_PHP_SESSION_ID,
			STD_J_SESSION_ID };

	// static constructor methods for typical cases

	/**
	 * create WaybackRequet for URL-Query request.
	 * @param url target URL
	 * @param start start timestamp (14-digit)
	 * @param end end timestamp (14-digit)
	 * @return WaybackRequest
	 */
	public static WaybackRequest createUrlQueryRequest(String url, String start, String end) {
		WaybackRequest r = new WaybackRequest();
		r.setUrlQueryRequest();
		r.setRequestUrl(url);
		r.setStartTimestamp(start);
		r.setEndTimestamp(end);
		return r;
	}

	/**
	 * create WaybackRequest for Capture-Query request.
	 * @param url target URL
	 * @param replay highlight date
	 * @param start start timestamp (14-digit)
	 * @param end end timestamp (14-digit)
	 * @return WaybackRequest
	 */
	public static WaybackRequest createCaptureQueryRequet(String url, String replay, String start, String end) {
		WaybackRequest r = new WaybackRequest();
		r.setCaptureQueryRequest();
		r.setRequestUrl(url);
		r.setReplayTimestamp(replay);
		r.setStartTimestamp(start);
		r.setEndTimestamp(end);
		return r;
	}
	/**
	 * create WaybackRequet for Replay request.
	 * @param url target URL
	 * @param replay requested date
	 * @param start start timestamp (14-digit)
	 * @param end end timestamp (14-digit)
	 * @return WaybackRequet
	 */
	public static WaybackRequest createReplayRequest(String url, String replay, String start, String end) {
		WaybackRequest r = new WaybackRequest();
		r.setReplayRequest();
		r.setRequestUrl(url);
		r.setReplayTimestamp(replay);
		r.setStartTimestamp(start);
		r.setEndTimestamp(end);
		return r;
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
	 * @deprecated use getAccessPoint.getStaticPrefix() or
	 * getAccessPoint.getReplayPrefix()
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
	 * @deprecated use AccessPoint.setReplayPrefix or setQueryPrefix
	 */
	public String getContextPrefix() {
		if(accessPoint == null) {
			return "";
		}
		return accessPoint.getQueryPrefix();
	}

	/**
	 * @param prefix
	 * @deprecated use AccessPoint.set*Prefix
	 */
	public void setServerPrefix(String prefix) {
		serverPrefix = prefix;
	}

	/**
	 * @param prefix
	 * @return an absolute String URL that will point to the root of the
	 * server that is handling the request. 
	 * @deprecated use AccessPoint.get*Prefix
	 */
	public String getServerPrefix() {
		if(accessPoint == null) {
			return "";
		}
		return accessPoint.getQueryPrefix();
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

	public ExclusionFilter getExclusionFilter() {
		return exclusionFilter;
	}

	public void setExclusionFilter(ExclusionFilter exclusionFilter) {
		this.exclusionFilter = exclusionFilter;
	}

	public void setResultFilters(ObjectFilterChain<CaptureSearchResult> resultFilters) {
		this.resultFilters = resultFilters;
	}

	public void addResultFilter(ObjectFilter<CaptureSearchResult> resultFilter) {
		if(resultFilters == null) {
			resultFilters = new ObjectFilterChain<CaptureSearchResult>();
		}
		resultFilters.addFilter(resultFilter);
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

	private int getInt(String key) {
		String value = get(key);
		if(value == null) {
			return -1;
		}
		return Integer.parseInt(value);
	}	
	private void setInt(String key, int value) {
		put(key,String.valueOf(value));
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
	 * marks this request as a Capture Query request
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
	 * marks this request as an Url Query request
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

		// This looks a little confusing: We're trying to fixup an incoming
		// request URL that starts with: 
		//       "http:/www.archive.org"
		// so it becomes:
		//       "http://www.archive.org"
		// (note the missing second "/" in the first)
		// 
		// if that is not the case, then see if the incoming scheme
		// is known, adding an implied "http://" scheme if there doesn't appear
		// to be a scheme..
		// TODO: make the default "http://" configurable.
		if (!urlStr.startsWith(UrlOperations.HTTP_SCHEME)) {
	    	if(urlStr.startsWith("http:/")) {
	    		urlStr = UrlOperations.HTTP_SCHEME + urlStr.substring(6);
	    	} else {
	    		if(UrlOperations.urlToScheme(urlStr) == null) {
	    			urlStr = UrlOperations.HTTP_SCHEME + urlStr;
	    		}
	    	}
	    }
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

	public void setExactScheme(boolean isExactScheme) {
		setBoolean(REQUEST_EXACT_SCHEME_ONLY,isExactScheme);
	}
	public boolean isExactScheme() {
		return getBoolean(REQUEST_EXACT_SCHEME_ONLY);
	}

	public void setLiveWebRequest(boolean isLiveWebRequest) {
		setInt(REQUEST_IS_LIVE_WEB, 1);
	}
	public void setLiveWebEmbedRequest(boolean isLiveWebEmbedRequest) {
		setInt(REQUEST_IS_LIVE_WEB, 2);
	}
	public boolean isLiveWebRequest() {
		return getInt(REQUEST_IS_LIVE_WEB) != -1;
	}
	public boolean isLiveWebEmbedRequest() {
		return getInt(REQUEST_IS_LIVE_WEB) == 2;
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

	public void setJSContext(boolean isJSContext) {
		setBoolean(REQUEST_JS_CONTEXT,isJSContext);
	}
	public boolean isJSContext() {
		return getBoolean(REQUEST_JS_CONTEXT);
	}

	public void setCSSContext(boolean isCSSContext) {
		setBoolean(REQUEST_CSS_CONTEXT,isCSSContext);
	}
	public boolean isCSSContext() {
		return getBoolean(REQUEST_CSS_CONTEXT);
	}
	
	public void setIMGContext(boolean isIMGContext) {
		setBoolean(REQUEST_IMAGE_CONTEXT,isIMGContext);
	}
	public boolean isIMGContext() {
		return getBoolean(REQUEST_IMAGE_CONTEXT);
	}

	public void setObjectEmbedContext(boolean isObjectEmbedContext) {
		setBoolean(REQUEST_OBJECT_EMBED_CONTEXT,isObjectEmbedContext);
	}
	public boolean isObjectEmbedContext() {
		return getBoolean(REQUEST_OBJECT_EMBED_CONTEXT);
	}
	
	public void setIdentityContext(boolean isIdentityContext) {
		setBoolean(REQUEST_IDENTITY_CONTEXT,isIdentityContext);
	}
	public boolean isIdentityContext() {
		return getBoolean(REQUEST_IDENTITY_CONTEXT);
	}

	public void setFrameWrapperContext(boolean isFrameWrapperContext) {
		setBoolean(REQUEST_FRAME_WRAPPER_CONTEXT,isFrameWrapperContext);
	}
	public boolean isFrameWrapperContext() {
		return getBoolean(REQUEST_FRAME_WRAPPER_CONTEXT);
	}

	public void setIFrameWrapperContext(boolean isIFrameWrapperContext) {
		setBoolean(REQUEST_IFRAME_WRAPPER_CONTEXT,isIFrameWrapperContext);
	}
	public boolean isIFrameWrapperContext() {
		return getBoolean(REQUEST_IFRAME_WRAPPER_CONTEXT);
	}
	
	public boolean isAnyEmbeddedContext()
	{
		return this.isCSSContext() || this.isIMGContext() || this.isJSContext() ||
			this.isFrameWrapperContext() || this.isIFrameWrapperContext() || this.isObjectEmbedContext();
	}

	public void setAjaxRequest(boolean isAjaxRequest) {
		setBoolean(REQUEST_AJAX_REQUEST,isAjaxRequest);
	}
	public boolean isAjaxRequest() {
		return getBoolean(REQUEST_AJAX_REQUEST);
	}
	
	public void setMementoTimemapFormat(String format) {
		put(REQUEST_MEMENTO_TIMEMAP, format);
	}
	public String getMementoTimemapFormat()
	{
		return get(REQUEST_MEMENTO_TIMEMAP);
	}
	public boolean isMementoTimemapRequest() {
		return get(REQUEST_MEMENTO_TIMEMAP) != null;
	}
	
	public void setMementoAcceptDatetime(boolean acceptDatetime) {
		setBoolean(REQUEST_MEMENTO_ACCEPT_DATETIME, acceptDatetime);
	}
	public boolean hasMementoAcceptDatetime() {
		return getBoolean(REQUEST_MEMENTO_ACCEPT_DATETIME);
	}
	
	public void setMementoTimegate() {
		setBoolean(REQUEST_MEMENTO_TIMEGATE, true);
	}
	public boolean isMementoTimegate() {
		return getBoolean(REQUEST_MEMENTO_TIMEGATE);
	}
	

	public void setCharsetMode(int mode) {
		setInt(REQUEST_CHARSET_MODE,mode);
	}
	public int getCharsetMode() {
		int mode = getInt(REQUEST_CHARSET_MODE);
		return (mode == -1) ? 0 : mode;
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
	 * Add timestamp as well as url key to optimize loading for only a certain time range
	 * However, may not find all revisit records
	 */
	public void setTimestampSearchKey(boolean timestampSearchKey) {
		setBoolean(REQUEST_TIMESTAMP_SEARCH_KEY, timestampSearchKey);
	}
	
	public boolean isTimestampSearchKey() {
		return getBoolean(REQUEST_TIMESTAMP_SEARCH_KEY);
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
	 * @throws BadQueryException 
	 */
	public void extractHttpRequestInfo(HttpServletRequest httpRequest) {
		
		putUnlessNull(REQUEST_REFERER_URL, httpRequest.getHeader("REFERER"));
		
		String remoteAddr = httpRequest.getHeader("X-Forwarded-For");
		if (remoteAddr == null) {
			remoteAddr = httpRequest.getRemoteAddr();
		}
		putUnlessNull(REQUEST_REMOTE_ADDRESS, remoteAddr);
		
		// Check for AJAX
		String x_req_with = httpRequest.getHeader("X-Requested-With");
		if (x_req_with != null) {
			if (x_req_with.equals("XMLHttpRequest")) {
				this.setAjaxRequest(true);
			}
		} else if (this.getRefererUrl() != null && httpRequest.getParameter("ajaxpipe") != null) {
			this.setAjaxRequest(true);
		}
		
		if (accessPoint != null && accessPoint.isEnableMemento()) {		
			// Check for Memento Accept-Datetime
			String acceptDateTime = httpRequest.getHeader(MementoUtils.ACCEPT_DATETIME);
			if (acceptDateTime != null) {
				this.setMementoAcceptDatetime(true);
			}
		}
		
		putUnlessNull(REQUEST_WAYBACK_HOSTNAME, httpRequest.getLocalName());
		putUnlessNull(REQUEST_AUTH_TYPE, httpRequest.getAuthType());
		putUnlessNull(REQUEST_REMOTE_USER, httpRequest.getRemoteUser());
		
		putUnlessNull(REQUEST_AUTHORIZATION,
				httpRequest.getHeader(REQUEST_AUTHORIZATION));
		putUnlessNull(REQUEST_WAYBACK_PORT, 
				String.valueOf(httpRequest.getLocalPort()));
		putUnlessNull(REQUEST_WAYBACK_CONTEXT, httpRequest.getContextPath());

		Locale l = null;
		if(accessPoint != null) {
			l = accessPoint.getLocale();
		}
		if(l == null) {
			l = httpRequest.getLocale();
		}
		setLocale(l);
		putUnlessNull(REQUEST_LOCALE_LANG,l.getDisplayLanguage());

		Cookie[] cookies = httpRequest.getCookies();
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				String name = cookie.getName();
				String value = cookie.getValue();
				String oldVal = get(name);
				if(oldVal == null || oldVal.length() == 0) {
					put(name,value);
				}
			}
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

		try {
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
				if(val == null) continue;
				if (queryString.length() > 0) {
					queryString.append(" ");
				}
                                key = URLEncoder.encode(key,"UTF-8");
                                val = URLEncoder.encode(val,"UTF-8");
				queryString.append(key + ":" + val);
			}
			String escapedQuery = queryString.toString();

			escapedQuery = URLEncoder.encode(escapedQuery, "UTF-8");

			return OpenSearchRequestParser.SEARCH_QUERY + "=" + escapedQuery + "&"
				+ OpenSearchRequestParser.SEARCH_RESULTS + "=" + numPerPage + "&"
				+ OpenSearchRequestParser.START_PAGE + "=" + pageNum;

		} catch (UnsupportedEncodingException e) {
			// oops.. what to do?
			e.printStackTrace();
                        throw new RuntimeException(e);
		}
	}

	public WaybackRequest clone() {
		WaybackRequest wbRequest = new WaybackRequest();

		wbRequest.resultsPerPage = resultsPerPage;

		wbRequest.pageNum = pageNum;
		
		wbRequest.contextPrefix = contextPrefix;
		wbRequest.serverPrefix = serverPrefix;

		wbRequest.formatter = formatter;
		wbRequest.accessPoint = accessPoint;

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

	public boolean isBestLatestReplayRequest() {
		return this.getBoolean(REQUEST_LATEST_BEST_REPLAY); 
	}
	
	public void setBestLatestReplayRequest() {
		this.setBoolean(REQUEST_LATEST_BEST_REPLAY, true);
	}
}
