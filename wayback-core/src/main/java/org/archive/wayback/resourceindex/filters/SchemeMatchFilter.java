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
import org.archive.wayback.util.url.UrlOperations;

/**
 * ObjectFilter which omits CaptureSearchResult objects if their scheme does not
 * match the specified scheme.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class SchemeMatchFilter implements ObjectFilter<CaptureSearchResult> {

	private String scheme = null;
	private QueryCaptureFilterGroup annotationTarget = null;

	/**
	 * @param hostname String of original host to match
	 */
	public SchemeMatchFilter(final String scheme) {
		this.scheme = scheme;
	}
	/**
	 * @param hostname String of original host to match
	 */
	public SchemeMatchFilter(final String scheme, 
			QueryCaptureFilterGroup annotationTarget) {
		this.scheme = scheme;
		this.annotationTarget = annotationTarget;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String captureScheme = UrlOperations.urlToScheme(r.getOriginalUrl());
		if(scheme == null) {
			if(captureScheme == null) {
				return FILTER_INCLUDE;
			} else {
				annotationTarget.addCloseMatch(r.getOriginalHost(),
						r.getOriginalUrl());
				return FILTER_EXCLUDE;
			}
		}

		if(scheme.equals(captureScheme)) {
			return FILTER_INCLUDE;
		} else {
			if(annotationTarget != null) {
				annotationTarget.addCloseMatch(r.getOriginalHost(),
						r.getOriginalUrl());
			}
			 return FILTER_EXCLUDE;
		}
	}
}
