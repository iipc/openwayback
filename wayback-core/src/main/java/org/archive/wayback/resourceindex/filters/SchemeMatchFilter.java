/* SchemeMatchFilter
 *
 * $Id$
 *
 * Created on 6:40:02 PM Nov 6, 2008.
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
