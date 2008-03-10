/* OpenSearchRequestParser
 *
 * $Id$
 *
 * Created on 4:47:03 PM Apr 24, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.requestparser;

import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class OpenSearchRequestParser extends BaseRequestParser {

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
		String query = getMapParam(queryMap, SEARCH_QUERY);
		if(query == null) {
			return null;
		}
		wbRequest = new WaybackRequest();
		
		String base = wbContext.translateRequestPath(httpRequest);
		if(base.startsWith(REPLAY_BASE)) {
			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_REPLAY_QUERY);
		} else if(base.startsWith(QUERY_BASE)){
			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_URL_QUERY);
		} else if(base.startsWith(XQUERY_BASE)){
			wbRequest.put(WaybackConstants.REQUEST_TYPE,
					WaybackConstants.REQUEST_URL_QUERY);
			wbRequest.put(WaybackConstants.REQUEST_XML_DATA,"1");
			
		} else {
			return null;
		}
		
		String numResults = getMapParam(queryMap, SEARCH_RESULTS);
		String startPage = getMapParam(queryMap, START_PAGE);

		if (numResults != null) {
			int nr = Integer.parseInt(numResults);
			wbRequest.setResultsPerPage(nr);
		} else {
			wbRequest.setResultsPerPage(maxRecords);
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
			String key = token.substring(0, colonIndex);
			String value = token.substring(colonIndex + 1);
			// TODO: make sure key is in singleTokens?
			// let's just let em all thru for now:
			wbRequest.put(key, value);
		}
		if(wbRequest.get(WaybackConstants.REQUEST_START_DATE) == null) {
			wbRequest.put(WaybackConstants.REQUEST_START_DATE, 
					getEarliestTimestamp());
		}
		if(wbRequest.get(WaybackConstants.REQUEST_END_DATE) == null) {
			wbRequest.put(WaybackConstants.REQUEST_END_DATE, 
					getLatestTimestamp());
		}
		wbRequest.fixup(httpRequest);

		return wbRequest;
	}
}
