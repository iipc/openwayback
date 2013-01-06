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
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.LocalResourceIndex;
import org.archive.wayback.resourceindex.filters.ConditionalGetAnnotationFilter;
import org.archive.wayback.resourceindex.filters.DuplicateHashFilter;
import org.archive.wayback.resourceindex.filters.WARCRevisitAnnotationFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class AnnotatingCaptureFilterGroup implements CaptureFilterGroup {

	private ObjectFilterChain<CaptureSearchResult> chain;

	public AnnotatingCaptureFilterGroup(LocalResourceIndex index) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		if(index.isDedupeRecords()) {
			chain.addFilter(new WARCRevisitAnnotationFilter());
			chain.addFilter(new ConditionalGetAnnotationFilter());
			//chain.addFilter(new DuplicateHashFilter());
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
