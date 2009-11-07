/* CoreCaptureFilterGroup
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
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filters.ConditionalGetAnnotationFilter;
import org.archive.wayback.resourceindex.filters.DuplicateRecordFilter;
import org.archive.wayback.resourceindex.filters.GuardRailFilter;
import org.archive.wayback.resourceindex.filters.WARCRevisitAnnotationFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class CoreCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;

	public CoreCaptureFilterGroup(LocalResourceIndex index) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		chain.addFilter(new GuardRailFilter(index.getMaxRecords()));
		chain.addFilter(new DuplicateRecordFilter());
		if(index.isDedupeRecords()) {
			chain.addFilter(new WARCRevisitAnnotationFilter());
			chain.addFilter(new ConditionalGetAnnotationFilter());
		}
	}
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results) {
		// TODO: ask guardRailFilter if it aborted processing (too many records)
		// and annotate the results with info about how to continue the request?
	}
}
