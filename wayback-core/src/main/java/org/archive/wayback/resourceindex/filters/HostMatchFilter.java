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
import org.archive.wayback.resourceindex.filterfactory.QueryCaptureFilterGroup;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which includes only records that have original host 
 * matching.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class HostMatchFilter implements ObjectFilter<CaptureSearchResult> {

	private String hostname = null;
	private QueryCaptureFilterGroup annotationTarget = null;
	
	/**
	 * @param hostname String of original host to match
	 */
	public HostMatchFilter(final String hostname,
			QueryCaptureFilterGroup annotationTarget) {
		this.hostname = hostname;
		this.annotationTarget = annotationTarget;
	}

	/**
	 * @param hostname String of original host to match
	 */
	public HostMatchFilter(final String hostname) {
		this.hostname = hostname;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String origHost = r.getOriginalHost();
		if(hostname.equals(origHost)) {
			return FILTER_INCLUDE;
		} else {
			if(annotationTarget != null) {
				annotationTarget.addCloseMatch(origHost, r.getOriginalUrl());
			}
			return FILTER_EXCLUDE;
		}
	}
}
