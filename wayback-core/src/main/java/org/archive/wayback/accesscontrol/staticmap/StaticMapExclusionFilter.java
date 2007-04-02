/* StaticExclusionFilter
 *
 * $Id$
 *
 * Created on 6:36:04 PM Mar 5, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.staticmap;

import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.SearchResultFilter;
import org.archive.wayback.surt.SURTTokenizer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StaticMapExclusionFilter extends SearchResultFilter {

	private String lastChecked = null;
	private boolean lastCheckedExcluded = false;
	Map exclusionMap = null;
	/**
	 * @param map
	 */
	public StaticMapExclusionFilter(Map map) {
		exclusionMap = map;
	}
	
	private boolean isExcluded(String url) {
		try {
			SURTTokenizer st = new SURTTokenizer(url);
			while(true) {
				String nextSearch = st.nextSearch();
				if(nextSearch == null) {
					break;
				}
				if(exclusionMap.containsKey(nextSearch)) {
					return true;
				}
			}
		} catch (URIException e) {
			e.printStackTrace();
			return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterSearchResult(SearchResult r) {
		String url = r.get(WaybackConstants.RESULT_URL);
		if(lastChecked != null) {
			if(lastChecked.equals(url)) {
				return lastCheckedExcluded ? 
						SearchResultFilter.FILTER_EXCLUDE : 
							SearchResultFilter.FILTER_INCLUDE;
			}
		}
		lastChecked = url;
		lastCheckedExcluded = isExcluded(url);
		return lastCheckedExcluded ? 
				SearchResultFilter.FILTER_EXCLUDE : 
					SearchResultFilter.FILTER_INCLUDE;
	}
}
