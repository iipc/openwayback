/* CaptureToUrlResultFilter
 *
 * $Id$
 *
 * Created on 6:23:07 PM Apr 19, 2007.
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
package org.archive.wayback.resourceindex.filters;

import java.util.HashMap;
import java.util.Properties;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CaptureToUrlResultFilter implements ObjectFilter<SearchResult> {
	private String currentUrl;
	private String firstCapture;
	private String lastCapture;
	private int numCaptures;
	private HashMap<String,Object> digests;
	private SearchResult resultRef = null;

	/**
	 * 
	 */
	public final static String RESULT_URL = "result.url";
	/**
	 * 
	 */
	public final static String RESULT_FIRST_CAPTURE = "result.firstcapture";
	/**
	 * 
	 */
	public final static String RESULT_LAST_CAPTURE = "result.lastcapture";
	/**
	 * 
	 */
	public final static String RESULT_NUM_CAPTURES = "result.numcaptures";
	/**
	 * 
	 */
	public final static String RESULT_NUM_VERSIONS = "result.numversions";
	/**
	 * 
	 */
	public final static String RESULT_ORIGINAL_URL = "result.originalurl";
	
	private void fungeSearchResult(SearchResult result) {
		String originalUrl = result.get(WaybackConstants.RESULT_URL);
		currentUrl = result.get(WaybackConstants.RESULT_URL_KEY);
		firstCapture = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		lastCapture = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		digests = new HashMap<String,Object>();
		digests.put(result.get(WaybackConstants.RESULT_MD5_DIGEST),null);
		numCaptures = 1;

		Properties p = result.getData();
		p.clear();
		resultRef = result;
		resultRef.put(RESULT_ORIGINAL_URL,originalUrl);
		resultRef.put(RESULT_URL,currentUrl);
		resultRef.put(RESULT_FIRST_CAPTURE,firstCapture);
		resultRef.put(RESULT_LAST_CAPTURE,lastCapture);
		resultRef.put(RESULT_NUM_CAPTURES,"1");
		resultRef.put(RESULT_NUM_VERSIONS,"1");
	}

	public int filterObject(SearchResult r) {
		String urlKey = r.get(WaybackConstants.RESULT_URL_KEY);
		if(resultRef == null || !currentUrl.equals(urlKey)) {
			fungeSearchResult(r);
			return FILTER_INCLUDE;
		}

		// same url -- accumulate:
		String captureDate = r.get(WaybackConstants.RESULT_CAPTURE_DATE);
		if(captureDate.compareTo(firstCapture) < 0) {
			firstCapture = captureDate;
			resultRef.put(RESULT_FIRST_CAPTURE,firstCapture);
		}
		if(captureDate.compareTo(lastCapture) > 0) {
			lastCapture = captureDate;
			resultRef.put(RESULT_LAST_CAPTURE,lastCapture);
		}
		numCaptures++;
		digests.put(r.get(WaybackConstants.RESULT_MD5_DIGEST), null);
		resultRef.put(RESULT_NUM_CAPTURES,String.valueOf(numCaptures));
		resultRef.put(RESULT_NUM_VERSIONS,String.valueOf(digests.size()));
		return FILTER_EXCLUDE;
	}

}
