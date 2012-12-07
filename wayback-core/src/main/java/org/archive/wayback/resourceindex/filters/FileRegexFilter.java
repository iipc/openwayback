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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class FileRegexFilter implements ObjectFilter<CaptureSearchResult> {

	protected Pattern patterns[] = null;
	
	public List<String> getPatterns() {
		ArrayList<String> s = new ArrayList<String>();
		for(Pattern p : patterns) {
			s.add(p.pattern());
		}
		return s;
	}

	public void setPatterns(List<String> patternStrings) {
		int size = patternStrings.size();
		patterns = new Pattern[size];
		for(int i = 0; i < size; i++) {
			patterns[i] = Pattern.compile(patternStrings.get(i));
		}
	}
	
	public int filterObject(CaptureSearchResult o) {
		final String file = o.getFile();
		for(Pattern pattern : patterns) {
			if(pattern.matcher(file).find()) {
				return FILTER_INCLUDE;
			}
		}
		return FILTER_EXCLUDE;
	}
}
