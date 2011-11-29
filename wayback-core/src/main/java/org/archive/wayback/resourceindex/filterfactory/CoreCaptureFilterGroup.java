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
package org.archive.wayback.resourceindex.filterfactory;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filters.DuplicateRecordFilter;
import org.archive.wayback.resourceindex.filters.GuardRailFilter;
import org.archive.wayback.resourceindex.filters.MimeTypeFilter;
import org.archive.wayback.resourceindex.filters.UserInfoInAuthorityFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class CoreCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private MimeTypeFilter mimeExcludeFilter = new MimeTypeFilter();
	private static String ALEXA_DAT_MIME = "alexa/dat";

	public CoreCaptureFilterGroup(LocalResourceIndex index) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		chain.addFilter(new GuardRailFilter(index.getMaxRecords()));
		chain.addFilter(new DuplicateRecordFilter());

		MimeTypeFilter mimeExcludeFilter = new MimeTypeFilter();
		mimeExcludeFilter.addMime(ALEXA_DAT_MIME);
		mimeExcludeFilter.setIncludeIfContains(false);
		chain.addFilter(new UserInfoInAuthorityFilter());
		chain.addFilter(mimeExcludeFilter);
	}
	public List<ObjectFilter<CaptureSearchResult>> getFilters() {
		return chain.getFilters();
	}

	public void annotateResults(SearchResults results) {
		// TODO: ask guardRailFilter if it aborted processing (too many records)
		// and annotate the results with info about how to continue the request?
	}
}
