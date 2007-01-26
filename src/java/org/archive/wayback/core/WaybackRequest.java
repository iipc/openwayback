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

import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.query.OpenSearchQueryParser;

/**
 * Abstraction of all the data associated with a users request to the Wayback
 * Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackRequest {

	private int resultsPerPage = 10;

	private int pageNum = 1;

	private Properties filters = new Properties();
	
	private ResourceBundle resourceBundle = null;
	private String UI_RESOURCE_BUNDLE_NAME = "WaybackUI";

	private final static String standardHeaders[] = {
			WaybackConstants.REQUEST_REFERER_URL,
			WaybackConstants.REQUEST_REMOTE_ADDRESS,
			WaybackConstants.REQUEST_WAYBACK_HOSTNAME,
			WaybackConstants.REQUEST_WAYBACK_PORT,
			WaybackConstants.REQUEST_WAYBACK_CONTEXT,
			WaybackConstants.REQUEST_AUTH_TYPE,
			WaybackConstants.REQUEST_REMOTE_USER };

	/**
	 * Constructor, possibly/probably this should BE a Properties, instead of
	 * HAVEing a Properties...
	 */
	public WaybackRequest() {
		super();
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
	
	private String getUserLocale(HttpServletRequest httpRequest) {
		resourceBundle = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME,
				httpRequest.getLocale());
		
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
		put(WaybackConstants.REQUEST_REFERER_URL, emptyIfNull(httpRequest
				.getHeader("REFERER")));
		put(WaybackConstants.REQUEST_REMOTE_ADDRESS, emptyIfNull(httpRequest
				.getRemoteAddr()));
		put(WaybackConstants.REQUEST_WAYBACK_HOSTNAME, emptyIfNull(httpRequest
				.getLocalName()));
		put(WaybackConstants.REQUEST_WAYBACK_PORT, String.valueOf(httpRequest
				.getLocalPort()));
		put(WaybackConstants.REQUEST_WAYBACK_CONTEXT, emptyIfNull(httpRequest
				.getContextPath()));
		put(WaybackConstants.REQUEST_AUTH_TYPE, emptyIfNull(httpRequest
				.getAuthType()));
		put(WaybackConstants.REQUEST_REMOTE_USER, emptyIfNull(httpRequest
				.getRemoteUser()));
		put(WaybackConstants.REQUEST_LOCALE_LANG,getUserLocale(httpRequest));
		// TODO: cookies...
	}

	/**
	 * Construct an absolute URL that points to the root of the context that
	 * recieved the request, including a trailing "/".
	 * 
	 * @return String absolute URL pointing to the Context root where the
	 *         request was revieved.
	 */
	public String getDefaultWaybackPrefix() {
		String waybackHostname = get(WaybackConstants.REQUEST_WAYBACK_HOSTNAME);
		String waybackPort = get(WaybackConstants.REQUEST_WAYBACK_PORT);
		String waybackContext = get(WaybackConstants.REQUEST_WAYBACK_CONTEXT);
		StringBuilder prefix = new StringBuilder();
		prefix.append(WaybackConstants.HTTP_URL_PREFIX);
		prefix.append(waybackHostname);
		if (waybackPort.compareTo(WaybackConstants.HTTP_DEFAULT_PORT) != 0) {
			prefix.append(":").append(waybackPort);
		}
		prefix.append("/");
		if (waybackContext != null && waybackContext.length() > 0) {
			prefix.append(waybackContext).append("/");
		}
		return prefix.toString();
	}

	/**
	 * attempt to fixup this WaybackRequest, mostly with respect to dates: if
	 * only "date" was specified, infer start and end dates from it. Also grab
	 * useful info from the HttpServletRequest, cookies, remote address, etc.
	 * 
	 * @param httpRequest
	 */
	public void fixup(HttpServletRequest httpRequest) {
		String startDate = get(WaybackConstants.REQUEST_START_DATE);
		String endDate = get(WaybackConstants.REQUEST_END_DATE);
		String exactDate = get(WaybackConstants.REQUEST_EXACT_DATE);
		String partialDate = get(WaybackConstants.REQUEST_DATE);
		if (partialDate == null) {
			partialDate = "";
		}
		if (startDate == null || startDate.length() == 0) {
			put(WaybackConstants.REQUEST_START_DATE, Timestamp
					.padStartDateStr(partialDate));
		} else if (startDate.length() < 14) {
			put(WaybackConstants.REQUEST_START_DATE, Timestamp
					.padStartDateStr(startDate));
		}
		if (endDate == null || endDate.length() == 0) {
			put(WaybackConstants.REQUEST_END_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (endDate.length() < 14) {
			put(WaybackConstants.REQUEST_END_DATE, Timestamp
					.padEndDateStr(endDate));
		}
		if (exactDate == null || exactDate.length() == 0) {
			put(WaybackConstants.REQUEST_EXACT_DATE, Timestamp
					.padEndDateStr(partialDate));
		} else if (exactDate.length() < 14) {
			put(WaybackConstants.REQUEST_EXACT_DATE, Timestamp
					.padEndDateStr(exactDate));
		}
		extractHttpRequestInfo(httpRequest);
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
		for (Enumeration e = filters.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			boolean isStandard = false;
			for(int i=0; i<standardHeaders.length; i++) {
				if(standardHeaders[i].compareTo(key) == 0) {
					isStandard = true;
					break;
				}
			}
			if(isStandard) continue;
			String val = (String) filters.get(key);
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
		return OpenSearchQueryParser.SEARCH_QUERY + "=" + escapedQuery + "&"
				+ OpenSearchQueryParser.SEARCH_RESULTS + "=" + numPerPage + "&"
				+ OpenSearchQueryParser.START_PAGE + "=" + pageNum;
	}

    /**
     * Set the request URL.
     * Also populates request url cleaned.
     * @param urlStr Request URL.
     * @throws URIException
     */
	public void setRequestUrl(String urlStr) throws URIException {
	    if (!urlStr.startsWith("http://")) {
	        urlStr = "http://" + urlStr;
	    }
        // If its not http, next line throws exception. TODO: Fix.
	    UURI requestURI = UURIFactory.getInstance(urlStr);
	    put(WaybackConstants.REQUEST_URL_CLEANED, requestURI.toString());
        put(WaybackConstants.REQUEST_URL, urlStr);
	}

	/**
	 * @param key
	 * @return localized String version of key argument, or key itself if
	 * something goes wrong...
	 */
	public String getLocalized(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (Exception e) {
			return key;
		}
	}
	
}