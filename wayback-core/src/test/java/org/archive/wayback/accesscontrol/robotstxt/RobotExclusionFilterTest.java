/* RobotExclusionFilterTest
 *
 * $Id$
 *
 * Created on 2:55:58 PM Mar 21, 2007.
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
