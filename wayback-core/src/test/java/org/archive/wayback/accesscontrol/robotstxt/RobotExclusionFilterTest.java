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
package org.archive.wayback.accesscontrol.robotstxt;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RobotExclusionFilterTest extends TestCase {
	
	/**
	 * 
	 */
	public void testFoo() {
		String re = "^www[0-9]+\\.";
		Pattern p = Pattern.compile(re);
		String url = "www4.archive.org";
		Matcher m = p.matcher(url);
		assertTrue(m.find());
		
	}
	
	/**
	 * 
	 */
	public void testSearchResultToRobotUrlStrings() {
		RobotExclusionFilter f = new RobotExclusionFilter(null,"",100);
		String test1[] = {"www.foo.com","foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www.foo.com"),test1);

		String test2[] = {"foo.com","www.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("foo.com"),test2);

		String test3[] = {"fool.foo.com","www.fool.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("fool.foo.com"),test3);

		String test4[] = {"www4.foo.com","www.foo.com","foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www4.foo.com"),test4);

		String test5[] = {"www4w.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www4w.foo.com"),test5);
		
		String test6[] = {"www.www.foo.com","www.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www.www.foo.com"),test6);
	}
	
	private void compareListTo(List<String> list, String strings[]) {
		assertEquals(list.size(), strings.length);
		for(int i = 0; i < strings.length; i++) {
			String listS = list.get(i);
			String arrayS = "http://" + strings[i] + "/robots.txt";
			assertEquals(listS, arrayS);
		}
	}
}
