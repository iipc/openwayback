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

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which includes any URL which begins with a given prefix, 
 * and aborts processing when any URL does not match the prefix. This abort
 * short-circuiting assumes that records will be seen in increasing URL order:
 * once a URL does not match, no further URLs will match either. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlPrefixMatchFilter implements ObjectFilter<CaptureSearchResult> {

	private String prefix;
	
	/**
	 * @param prefix String which records must begin with
	 */
	public UrlPrefixMatchFilter(final String prefix) {
		this.prefix = prefix;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String resultUrl = r.getUrlKey();
		return resultUrl.startsWith(prefix)	? FILTER_INCLUDE : FILTER_ABORT;
	}
}
