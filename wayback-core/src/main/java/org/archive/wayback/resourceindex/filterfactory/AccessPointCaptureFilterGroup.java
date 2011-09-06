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
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.filters.DateEmbargoFilter;
import org.archive.wayback.resourceindex.filters.FilePrefixFilter;
import org.archive.wayback.resourceindex.filters.FileRegexFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.CustomResultFilterFactory;

public class AccessPointCaptureFilterGroup implements CaptureFilterGroup {
	private ObjectFilterChain<CaptureSearchResult> chain = null;
	private final static String[] sA = new String[0];

	public AccessPointCaptureFilterGroup(WaybackRequest request) {
		chain = new ObjectFilterChain<CaptureSearchResult>();
		AccessPoint accessPoint = request.getAccessPoint();
		List<String> prefixes = null;
		if(request.getAccessPoint() != null) {
			prefixes = accessPoint.getFileIncludePrefixes();
			if(prefixes != null && prefixes.size() > 0) {
				FilePrefixFilter f = new FilePrefixFilter();
				f.setPrefixes(prefixes.toArray(sA));
				chain.addFilter(f);
			}
			prefixes = accessPoint.getFileExcludePrefixes();
			if(prefixes != null && prefixes.size() > 0) {
				FilePrefixFilter f = new FilePrefixFilter();
				f.setIncludeMatches(false);
				f.setPrefixes(prefixes.toArray(sA));
				chain.addFilter(f);
			}
			
			
			List<String> patterns = accessPoint.getFilePatterns();
			if(patterns != null && patterns.size() > 0) {
				FileRegexFilter f = new FileRegexFilter();
				f.setPatterns(patterns);
				chain.addFilter(f);
			}
			long embargoMS = accessPoint.getEmbargoMS();
			if(embargoMS > 0) {
				chain.addFilter(new DateEmbargoFilter(embargoMS));
			}
			CustomResultFilterFactory factory = accessPoint.getFilterFactory();
			if(factory != null) {
				ObjectFilter<CaptureSearchResult> filter = 
					factory.get(accessPoint);
				if(filter != null) {
					chain.addFilter(filter);
				}
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
