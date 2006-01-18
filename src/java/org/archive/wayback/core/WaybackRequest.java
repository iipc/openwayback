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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.archive.wayback.query.OpenSearchQueryParser;

/**
 * Abstraction of all the data associated with a users request to the Wayback
 * Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackRequest {
	
	private int resultsPerPage = 1000;
	private int pageNum = 1;
	private Properties filters = new Properties();
	
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
	 * @param pageNum The pageNum to set.
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
	 * @param resultsPerPage The resultsPerPage to set.
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
	
	/**
	 * @return String hex-encoded GET CGI arguments which will duplicate this
	 * wayback request
	 */
	public String getQueryArguments () {
		return getQueryArguments(pageNum);
	}
	
	/**
	 * @param pageNum
	 * @return String hex-encoded GET CGI arguments which will duplicate the
	 * same request, but for page 'pageNum' of the results  
	 */
	public String getQueryArguments (int pageNum) {
		int numPerPage = resultsPerPage;

		StringBuffer queryString = new StringBuffer("");
		for (Enumeration e = filters.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String val = (String) filters.get(key);
			if(queryString.length() > 0) {
				queryString.append(" ");
			}
			queryString.append(key+":"+val);
		}
		String escapedQuery = queryString.toString();

		try {
			
			escapedQuery = URLEncoder.encode(escapedQuery,"UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			// oops.. what to do?
			e.printStackTrace();
		}
		return OpenSearchQueryParser.SEARCH_QUERY + "=" + escapedQuery + 
			"&" + OpenSearchQueryParser.SEARCH_RESULTS + "=" + numPerPage +
			"&" + OpenSearchQueryParser.START_PAGE + "=" + pageNum;
	}
}
