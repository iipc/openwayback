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

import java.util.Map;
import java.util.regex.Pattern;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class OpenSearchQueryParser {
	private final static String SEARCH_QUERY = "q";

	private final static String SEARCH_RESULTS = "count";

	private final static String START_PAGE = "start_page";

	// private final static String START_INDEX = "start_index";

	private final static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	// singles consume the next non-whitespace token following the term
	private String[] singleTokens = { "url", "site", "mimetype", "noredirect" };

	// lines consume the entire rest of the query
	private String[] lineTokens = { "terms" };

	private String getMapParam(Map queryMap, String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	public WaybackRequest parseQuery(Map queryMap) throws BadQueryException {
		WaybackRequest wbRequest = new WaybackRequest();
		String query = getMapParam(queryMap, SEARCH_QUERY);
		String numResults = getMapParam(queryMap, SEARCH_RESULTS);
		String startPage = getMapParam(queryMap, START_PAGE);
		if (numResults != null) {
			int nr = Integer.parseInt(numResults);
			wbRequest.setResultsPerPage(nr);
		}
		if (startPage != null) {
			int sp = Integer.parseInt(startPage);
			wbRequest.setPageNum(sp);
		}
		if (query == null) {
			throw new BadQueryException("No search query argument");
		}
		parseTerms(wbRequest, query);
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
