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
import org.apache.log4j.Logger;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.ObjectFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StaticMapExclusionFilter extends ExclusionFilter {
	private static final Logger LOGGER = Logger.getLogger(
			StaticMapExclusionFilter.class.getName());

	private String lastChecked = null;
	private boolean lastCheckedExcluded = false;
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;
	Map<String,Object> exclusionMap = null;
	/**
	 * @param map where each String key is a SURT that is blocked.
	 */
	public StaticMapExclusionFilter(Map<String,Object> map) {
		exclusionMap = map;
	}
	
	protected boolean isExcluded(String url) {
		try {
			SURTTokenizer st = new SURTTokenizer(url);
			while(true) {
				String nextSearch = st.nextSearch();
				if(nextSearch == null) {
					break;
				}
				LOGGER.trace("EXCLUSION-MAP:Checking " + nextSearch);
				if(exclusionMap.containsKey(nextSearch)) {
					LOGGER.info("EXCLUSION-MAP: EXCLUDED: \"" + nextSearch + "\" (" + url +")");
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
	public int filterObject(CaptureSearchResult r) {
		if(!notifiedSeen) { 
			filterGroup.setSawAdministrative();
			notifiedSeen = true;
		}
		String url = r.getOriginalUrl();
		if(lastChecked != null) {
			if(lastChecked.equals(url)) {
				if(lastCheckedExcluded) { 
					return ObjectFilter.FILTER_EXCLUDE;
				} else {
					// don't need to: already did last time...
					//filterGroup.setPassedAdministrative();
					return ObjectFilter.FILTER_INCLUDE;
				}
			}
		}
		lastChecked = url;
		lastCheckedExcluded = isExcluded(url);
		if(lastCheckedExcluded) {
			return ObjectFilter.FILTER_EXCLUDE;
		} else {
			if(!notifiedPassed) {
				filterGroup.setPassedAdministrative();
				notifiedPassed = true;
			}
			return ObjectFilter.FILTER_INCLUDE;
		}
			
	}
}
