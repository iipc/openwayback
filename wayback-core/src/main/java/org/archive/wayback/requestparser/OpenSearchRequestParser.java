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
package org.archive.wayback.requestparser;

import java.util.Map;
import java.util.regex.Pattern;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser which attempts to extract data from an HTML form, that is, from
 * HTTP GET request arguments containing a query, an optional count (results 
 * per page), and an optional current page argument. All other reqeust fields
 * are expected to be encoded within the query ("q") field.
 *
 * @author brad
 */
public class OpenSearchRequestParser extends WrappedRequestParser {

	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public OpenSearchRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * CGI argument name for query arguments
	 */
	public final static String SEARCH_QUERY = "q";

	/**
	 * CGI argument name for number of results per page, 1 based
	 */
	public final static String SEARCH_RESULTS = "count";

	/**
	 * CGI argument name for page number of results, 1 based
	 */
	public final static String START_PAGE = "start_page";

	
	// private final static String START_INDEX = "start_index";

	private final static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	// singles consume the next non-whitespace token following the term
	// private String[] singleTokens = { "url", "site", "mimetype", "noredirect" };

	// lines consume the entire rest of the query
	private String[] lineTokens = { "terms" };
	
	/*
	 * If the request includes a 'q' (query) argument, treat the request
	 * as an OpenSearch query, and extract all query terms, plus pagination 
	 * info from the httpRequest object.
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
		AccessPoint wbContext) throws BadQueryException {
		
		WaybackRequest wbRequest = null;
		@SuppressWarnings("unchecked")
		Map<String,String[]> queryMap = httpRequest.getParameterMap();
		String query = AccessPoint.getMapParam(queryMap, SEARCH_QUERY);
		if (query == null) {
			return null;
		}
		wbRequest = new WaybackRequest();
		
		String base = wbContext.translateRequestPath(httpRequest);
		if (base.startsWith(REPLAY_BASE)) {
			wbRequest.setReplayRequest();
		} else if(base.startsWith(QUERY_BASE)){
			wbRequest.setCaptureQueryRequest();
		} else if(base.startsWith(XQUERY_BASE)){
			wbRequest.setCaptureQueryRequest();
			wbRequest.setXMLMode(true);
			
		} else {
			return null;
		}
		
		String numResults = AccessPoint.getMapParam(queryMap, SEARCH_RESULTS);
		String startPage = AccessPoint.getMapParam(queryMap, START_PAGE);

		if (numResults != null) {
			int nr = Integer.parseInt(numResults);
			wbRequest.setResultsPerPage(nr);
		} else {
			wbRequest.setResultsPerPage(getMaxRecords());
		}
		if (startPage != null) {
			int sp = Integer.parseInt(startPage);
			wbRequest.setPageNum(sp);
		} else {
			wbRequest.setPageNum(1);
		}

		// first try the entire line_tokens:
		for (int i = 0; i < lineTokens.length; i++) {
			String token = lineTokens[i] + ":";
			int index = query.indexOf(token);
			if (index > -1) {
				// found it, take value as the remainder of the query
				String value = query.substring(index + token.length());
				// TODO: trim trailing whitespace?
				wbRequest.put(lineTokens[i], value);
				query = query.substring(0, index);
			}
		}

		// now split whatever is left on whitespace:
		String[] parts = WHITESPACE_PATTERN.split(query);
		for (int i = 0; i < parts.length; i++) {
			String token = parts[i];
			int colonIndex = token.indexOf(":");
			if (colonIndex == -1) {
				throw new BadQueryException("Bad search token(" + token + ")");
			}
			try {
				String key = URLDecoder.decode(token.substring(0, colonIndex),
						"UTF-8");
				String value = URLDecoder.decode(
						token.substring(colonIndex + 1), "UTF-8");
				// TODO: make sure key is in singleTokens?
				// let's just let em all thru for now:
				wbRequest.put(key, value);
			} catch (UnsupportedEncodingException e) {
				throw new BadQueryException("Unsupported encoding: UTF-8");
			}
		}
		if (wbRequest.getStartTimestamp() == null) {
			wbRequest.setStartTimestamp(getEarliestTimestamp());
		}
		if (wbRequest.getEndTimestamp() == null) {
			wbRequest.setEndTimestamp(getLatestTimestamp());
		}

		return wbRequest;
	}
}
