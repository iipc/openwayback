/* UICaptureQueryResults
 *
 * $Id$
 *
 * Created on 6:14:06 PM Jun 27, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UICaptureQueryResults extends UIQueryResults {
	
	private CaptureSearchResults results;
	private Timestamp firstResultTimestamp;

	private Timestamp lastResultTimestamp;

	/**
	 * Constructor -- chew search result summaries into format easier for JSPs
	 * to digest.
	 *  
	 * @param httpRequest 
	 * @param wbRequest 
	 * @param results
	 * @param uriConverter 
	 */
	public UICaptureQueryResults(HttpServletRequest httpRequest, 
			WaybackRequest wbRequest, CaptureSearchResults results,
			ResultURIConverter uriConverter) {
		super(httpRequest, wbRequest, results, uriConverter);

		this.firstResultTimestamp = Timestamp.parseBefore(results
				.getFirstResultTimestamp());
		this.lastResultTimestamp = Timestamp.parseBefore(results
				.getLastResultTimestamp());

		this.results = results;
	}

	/**
	 * @return first Timestamp in returned ResourceResults
	 */
	public Timestamp getFirstResultTimestamp() {
		return firstResultTimestamp;
	}

	/**
	 * @return last Timestamp in returned ResourceResults
	 */
	public Timestamp getLastResultTimestamp() {
		return lastResultTimestamp;
	}

	/**
	 * @return Iterator of CaptureSearchResult
	 */
	public Iterator<CaptureSearchResult> resultsIterator() {
		return results.iterator();
	}

	/**
	 * @return Returns the results.
	 */
	public CaptureSearchResults getResults() {
		return results;
	}
}
