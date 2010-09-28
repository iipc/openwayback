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
package org.archive.wayback.resourceindex.filters;

import java.util.HashMap;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which includes only records matching one or more supplied
 * Mime-Types. All comparision is case-insensitive.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeFilter implements ObjectFilter<CaptureSearchResult> {
	private HashMap<String,Integer> validMimes = null;
	private boolean includeIfContains = true; 
	
	/**
	 * @param mime String which is valid match for mime-type field
	 */
	public void addMime(final String mime) {
		if(validMimes == null) {
			validMimes = new HashMap<String, Integer>();
		}
		validMimes.put(mime.toLowerCase(),null);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String mime = r.getMimeType().toLowerCase();
		return validMimes.containsKey(mime) == includeIfContains ? 
				FILTER_INCLUDE : FILTER_EXCLUDE;
	}

	/**
	 * @return the includeIfContains
	 */
	public boolean isIncludeIfContains() {
		return includeIfContains;
	}

	/**
	 * @param includeIfContains the includeIfContains to set
	 */
	public void setIncludeIfContains(boolean includeIfContains) {
		this.includeIfContains = includeIfContains;
	}
}
