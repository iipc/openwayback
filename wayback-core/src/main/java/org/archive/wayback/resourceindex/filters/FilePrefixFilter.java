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

public class FilePrefixFilter implements ObjectFilter<CaptureSearchResult> {

	private String prefixes[] = null;
	private boolean includeMatches = true;
	
	
	public String[] getPrefixes() {
		return prefixes;
	}
	public void setPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
	}
	
	public int filterObject(CaptureSearchResult o) {
		final String file = o.getFile();
		for(String prefix : prefixes) {
			if(file.startsWith(prefix)) {
				return includeMatches ? FILTER_INCLUDE : FILTER_EXCLUDE;
			}
		}
		return includeMatches ? FILTER_EXCLUDE : FILTER_INCLUDE;
	}

	public boolean isIncludeMatches() {
		return includeMatches;
	}

	public void setIncludeMatches(boolean includeMatches) {
		this.includeMatches = includeMatches;
	}
}
