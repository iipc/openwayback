/* SearchResults
 *
 * $Id$
 *
 * Created on 12:52:13 PM Nov 9, 2005.
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
package org.archive.wayback.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.archive.wayback.WaybackConstants;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResults {
	private ArrayList results = null;
	private String firstResultDate;
	private String lastResultDate;
	private Properties filters = new Properties();
	
	public SearchResults() {
		super();
		results = new ArrayList();
	}
	/**
	 * @return true if no SearchResult objects, false otherwise.
	 */
	public boolean isEmpty() {
		return results.isEmpty();
	}
	/**
	 * @param result
	 *            SearchResult to add to this set
	 */
	public void addSearchResult(final SearchResult result) {
		String resultDate = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		if((firstResultDate == null) || 
				(firstResultDate.compareTo(resultDate) < 0)) {
			firstResultDate = resultDate;
		}
		if((lastResultDate == null) || 
				(lastResultDate.compareTo(resultDate) > 0)) {
			lastResultDate = resultDate;
		}
		results.add(result);
	}
	
	/**
	 * @return number of SearchResult objects contained in these SearchResults
	 */
	public int getResultCount() {
		return results.size();
	}
	
	/**
	 * @return an Iterator that contains the ResourceResult objects
	 */
	public Iterator iterator() {
		return results.iterator();
	}
	/**
	 * @return Returns the firstResultDate.
	 */
	public String getFirstResultDate() {
		return firstResultDate;
	}
	/**
	 * @return Returns the lastResultDate.
	 */
	public String getLastResultDate() {
		return lastResultDate;
	}

	public boolean containsFilter(String key) {
		return filters.containsKey(key);
	}

	public String getFilter(String key) {
		return (String) filters.get(key);
	}

	public String putFilter(String key, String value) {
		return (String) filters.put(key, value);
	}
}
