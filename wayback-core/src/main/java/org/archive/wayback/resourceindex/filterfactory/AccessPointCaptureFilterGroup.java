/* AccessPointCaptureFilterGroup
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.FilePrefixFilter;
import org.archive.wayback.resourceindex.filters.FileRegexFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class AccessPointCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private final static String[] sA = null;

	public AccessPointCaptureFilterGroup(WaybackRequest request) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		List<String> prefixes = null;
		if(request.getAccessPoint() != null) {
			prefixes = request.getAccessPoint().getFilePrefixes();
			if(prefixes != null && prefixes.size() > 0) {
				FilePrefixFilter f = new FilePrefixFilter();
				f.setPrefixes(prefixes.toArray(sA));
				chain.addFilter(f);
			}
			List<String> patterns = request.getAccessPoint().getFilePatterns();
			if(patterns != null && patterns.size() > 0) {
				FileRegexFilter f = new FileRegexFilter();
				f.setPatterns(patterns);
				chain.addFilter(f);
			}
		}
	}
	
	public void annotateResults(SearchResults results)
			throws ResourceNotInArchiveException, BadQueryException,
			AccessControlException {

	}

	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}
}
