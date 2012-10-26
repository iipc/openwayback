/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.accesscontrol.staticmap;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroup;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

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
	UrlCanonicalizer canonicalizer = null;
	/**
	 * @param map where each String key is a SURT that is blocked.
	 */
	public StaticMapExclusionFilter(Map<String,Object> map, UrlCanonicalizer canonicalizer) {
		exclusionMap = map;
		this.canonicalizer = canonicalizer;
	}
	
	
	// Set the canonicalizer from the filter, as it may be different from the default
	@Override
	public void setFilterGroup(ExclusionCaptureFilterGroup filterGroup) {
		super.setFilterGroup(filterGroup);
		if ((filterGroup != null) && (filterGroup.getCaptureFilterGroupCanonicalizer() != null)) {
			this.canonicalizer = filterGroup.getCaptureFilterGroupCanonicalizer();
		}
	}
	
	protected boolean isExcluded(String url) {
		try {
			SURTTokenizer st = new SURTTokenizer(url, canonicalizer.isSurtForm());
			while(true) {
				String nextSearch = st.nextSearch();
				if(nextSearch == null) {
					break;
				}
				LOGGER.fine("EXCLUSION-MAP:Checking " + nextSearch);
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
			if(filterGroup != null) {
				filterGroup.setSawAdministrative();
			}
			notifiedSeen = true;
		}
		String url = r.getUrlKey();
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
				if(filterGroup != null) {
					filterGroup.setPassedAdministrative();
				}
				notifiedPassed = true;
			}
			return ObjectFilter.FILTER_INCLUDE;
		}
			
	}
}
