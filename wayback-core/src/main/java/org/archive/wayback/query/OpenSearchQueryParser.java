/* OpenSearchParser
 *
 * $Id$
 *
 * Created on 1:37:19 PM Nov 14, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.query;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ConfigurationException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class OpenSearchQueryParser implements PropertyConfigurable {
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

	/**
	 * CGI argument name for Submit buttom...
	 */
	public final static String SUBMIT_BUTTON = "Submit";

	private final static int DEFAULT_MAX_RECORDS = 1000;

	private int maxRecords = DEFAULT_MAX_RECORDS;
	
	
	// private final static String START_INDEX = "start_index";

	private final static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	// singles consume the next non-whitespace token following the term
	// private String[] singleTokens = { "url", "site", "mimetype", "noredirect" };

	// lines consume the entire rest of the query
	private String[] lineTokens = { "terms" };

	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		String max = p.getProperty(WaybackConstants.RESULTS_PER_PAGE_CONFIG_NAME);
		if(max != null) {
			maxRecords = Integer.parseInt(max);
		}
	}	
	
	private String getMapParam(Map queryMap, String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}
	/**
	 * Extract user request from an OpenSearch query, transforming into a
	 * WaybackRequest object. For the moment (as a sort of hack) this will
	 * also attempt to extract request information from the "advanced search 
	 * form", where individual filters are individual CGI arguments, instead of
	 * being encoded together in the "q" argument.
	 * 
	 * @param httpRequest
	 * @return Wayback request representing user query, or null if not parseable
	 * @throws BadQueryException
	 */
	public WaybackRequest parseQuery(HttpServletRequest httpRequest) throws BadQueryException {
		Map queryMap = httpRequest.getParameterMap();
		WaybackRequest wbRequest = new WaybackRequest();
		String query = getMapParam(queryMap, SEARCH_QUERY);
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
		if (query == null) {
			wbRequest = parseForm(wbRequest,queryMap);
		} else {
			parseTerms(wbRequest, query);
		}

		wbRequest.fixup(httpRequest);
		return wbRequest;
	}
	
	private WaybackRequest parseForm(WaybackRequest wbRequest, Map queryMap) {
		Set keys = queryMap.keySet();
		Iterator itr = keys.iterator();
		while(itr.hasNext()) {
			String key = (String) itr.next();
			if(key.equals(SEARCH_RESULTS) || key.equals(START_PAGE) || 
					key.equals(SUBMIT_BUTTON)) {
				continue;
			}
			// just jam everything else in:
			String val = getMapParam(queryMap,key);
			wbRequest.put(key,val);
		}
		return wbRequest;
	}

	private void parseTerms(WaybackRequest wbRequest, String query)
			throws BadQueryException {

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
	}
}
