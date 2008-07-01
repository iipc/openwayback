/* UIUrlQueryResults
 *
 * $Id$
 *
 * Created on 6:01:39 PM Jun 27, 2008.
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
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIUrlQueryResults extends UIQueryResults {
	
	private UrlSearchResults results;

	/**
	 * Constructor -- chew search result summaries into format easier for JSPs
	 * to digest.
	 *  
	 * @param httpRequest 
	 * @param wbRequest 
	 * @param results
	 * @param uriConverter 
	 */
	public UIUrlQueryResults(HttpServletRequest httpRequest, 
			WaybackRequest wbRequest, UrlSearchResults results,
			ResultURIConverter uriConverter) {
		super(httpRequest, wbRequest, results, uriConverter);
		
		this.results = results;
	}

	/**
	 * @return Iterator of ResourceResults
	 */
	public Iterator<UrlSearchResult> resultsIterator() {
		return results.iterator();
	}

	/**
	 * @return Returns the results.
	 */
	public UrlSearchResults getResults() {
		return results;
	}
}
