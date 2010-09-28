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
import java.util.Arrays;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

import junit.framework.TestCase;

public class FileRegexFilterTest extends TestCase {
	String[] patterns = {"^one-", "^two-"};

	public void testGetSetPatterns() {
		FileRegexFilter f = new FileRegexFilter();
		List<String> in = Arrays.asList(patterns);
		f.setPatterns(in);
		List<String> out = f.getPatterns();
		assertTrue(listCmp(in,out));
	}

	public void testFilterObject() {
		List<String> in = Arrays.asList(patterns);
		FileRegexFilter f = new FileRegexFilter();
		f.setPatterns(in);
		CaptureSearchResult c = new CaptureSearchResult();
		c.setFile("one-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_INCLUDE);
		c.setFile("onedd-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_EXCLUDE);
		c.setFile("two-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_INCLUDE);
		f.setPatterns(new ArrayList<String>());
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_EXCLUDE);
	}
	private boolean listCmp(List<String> one, List<String> two) {
		if(one.size() != two.size()) {
			return false;
		}
		int size = one.size();
		for(int i = 0; i < size; i++) {
			if(!one.get(i).equals(two.get(i))) {
				return false;
			}
		}
		return true;
	}
}
