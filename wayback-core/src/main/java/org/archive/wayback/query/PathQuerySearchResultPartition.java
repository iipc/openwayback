/* PathQuerySearchResultPartition
 *
 * $Id$
 *
 * Created on 4:10:12 PM Apr 16, 2007.
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
package org.archive.wayback.query;

import java.util.Date;
import java.util.HashMap;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;

/**
 * UI Utility object for storing information about multiple captures of an URL.
 * This object tracks the url in question, total number of captures of that URL,
 * the first Date captured, the last Date captured, and the unique Digests for
 * all captures. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathQuerySearchResultPartition {
	private SearchResult firstResult = null;
	private int numResults;
	private Date firstDate;
	private Date lastDate;
	private String url;
	private HashMap<String,Object> digests;
	private ResultURIConverter uriConverter = null;
	/**
	 * Constructor
	 * @param result
	 * @param uriConverter 
	 */
	public PathQuerySearchResultPartition(SearchResult result,
			ResultURIConverter uriConverter) {
		firstResult = result;
		digests = new HashMap<String,Object>();
		digests.put(searchResultToDigest(result),null);
		url = searchResultToCanonicalUrl(result);
		numResults = 1;
		firstDate = searchResultToDate(result);
		lastDate = searchResultToDate(result);
		this.uriConverter = uriConverter;
	}
	
	/**
	 * @return String Url that will result in a query for the URL
	 * stored in this object
	 */
	public String queryUrl() {
		return uriConverter.makeQueryURI(firstResult);
	}
	/**
	 * @return String Url that will replay the first captured stored in this
	 * object
	 */
	public String replayUrl() {
		return uriConverter.makeReplayURI(firstResult);
	}
	
	/**
	 * incorporate data from the argument SearchResult into the aggregate
	 * statistics held in this object
	 * @param result
	 */
	public void addSearchResult(SearchResult result) {
		numResults++;
		Date d = searchResultToDate(result);
		String digest = result.get(WaybackConstants.RESULT_MD5_DIGEST);
		if(d.getTime() < firstDate.getTime()) {
			firstDate = d;
		}
		if(d.getTime() > lastDate.getTime()) {
			lastDate = d;
		}
		digests.put(digest, null);
	}
	/**
	 * @param result
	 * @return true if the argument SearchResult is for the same url as this
	 *         ResultPartition is storing
	 */
	public boolean sameUrl(SearchResult result) {
		return url.equals(searchResultToCanonicalUrl(result));
	}

	/**
	 * @return the firstDate
	 */
	public Date getFirstDate() {
		return firstDate;
	}
	/**
	 * @return the lastDate
	 */
	public Date getLastDate() {
		return lastDate;
	}
	/**
	 * @return the numResults
	 */
	public int getNumResults() {
		return numResults;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return the number of unique digests for this url
	 */
	public int getNumVersions() {
		return digests.size();
	}

	private static String searchResultToCanonicalUrl(SearchResult result) {
		return result.get(WaybackConstants.RESULT_URL_KEY);
	}
	private static String searchResultToDigest(SearchResult result) {
		return result.get(WaybackConstants.RESULT_MD5_DIGEST);
	}
	private static Date searchResultToDate(SearchResult result) {
		Timestamp t = new Timestamp(result.get(
				WaybackConstants.RESULT_CAPTURE_DATE));
		return t.getDate();
	}
}
