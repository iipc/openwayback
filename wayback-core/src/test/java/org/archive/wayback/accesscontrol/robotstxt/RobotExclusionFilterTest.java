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

import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.archive.wayback.accesscontrol.robotstxt.redis.RobotsTxtResource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.easymock.EasyMock;

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
	protected final static String HTTP_PREFIX = "http://";
	
	public void testSearchResultToRobotUrlStrings() {
		RobotExclusionFilter f = new RobotExclusionFilter(null,"",100);
		String test1[] = {"www.foo.com","foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www.foo.com", HTTP_PREFIX),test1);

		String test2[] = {"foo.com","www.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("foo.com", HTTP_PREFIX),test2);

		String test3[] = {"fool.foo.com","www.fool.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("fool.foo.com", HTTP_PREFIX),test3);

		String test4[] = {"www.foo.com","foo.com", "www4.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www4.foo.com", HTTP_PREFIX),test4);

		String test5[] = {"www4w.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www4w.foo.com", HTTP_PREFIX),test5);
		
		String test6[] = {"www.www.foo.com","www.foo.com"};
		compareListTo(f.searchResultToRobotUrlStrings("www.www.foo.com", HTTP_PREFIX),test6);
	}
	
	private void compareListTo(List<String> list, String strings[]) {
		assertEquals(list.size(), strings.length);
		for(int i = 0; i < strings.length; i++) {
			String listS = list.get(i);
			String arrayS = "http://" + strings[i] + "/robots.txt";
			assertEquals(listS, arrayS);
		}
	}

	public void testGetRules_403() throws Exception {
		CaptureSearchResult result = new CaptureSearchResult();
		result.setOriginalUrl("http://example.com/index.html");

		final URL rturl = new URL("http://example.com/robots.txt");
		final int STATUSCODE = 403;
		// non essential
		final long MAX_CACHE_MS = 1000;
		final String USER_AGENT = "ia_archiver";
		LiveWebCache cache = EasyMock.createMock(LiveWebCache.class);
		EasyMock.expect(cache.getCachedResource(rturl, MAX_CACHE_MS, true))
			.andThrow(new LiveDocumentNotAvailableException(rturl, STATUSCODE));
		// shall make no other getCachedResource calls,
		// specifically for "http://www.example.com/index.html"
		RobotExclusionFilter cut = new RobotExclusionFilter(cache, USER_AGENT,
			MAX_CACHE_MS);

		EasyMock.replay(cache);

		RobotRules rules = cut.getRules(result);

		assertNotNull(rules);

		assertTrue("rules is empty", rules.getUserAgentsFound().isEmpty());

		EasyMock.verify(cache);
	}

	public void testGetRules_502() throws Exception {
		CaptureSearchResult result = new CaptureSearchResult();
		result.setOriginalUrl("http://example.com/index.html");

		final URL rturl = new URL("http://example.com/robots.txt");
		final int STATUSCODE = 502;
		// non essential
		final long MAX_CACHE_MS = 1000;
		final String USER_AGENT = "ia_archiver";
		LiveWebCache cache = EasyMock.createMock(LiveWebCache.class);
		EasyMock.expect(cache.getCachedResource(rturl, MAX_CACHE_MS, true))
			.andThrow(new LiveDocumentNotAvailableException(rturl, STATUSCODE));
		// shall make no other getCachedResource calls,
		// specifically for "http://www.example.com/index.html"
		RobotExclusionFilter cut = new RobotExclusionFilter(cache, USER_AGENT,
			MAX_CACHE_MS);

		EasyMock.replay(cache);

		RobotRules rules = cut.getRules(result);

		// null means full-disallow.
		assertNull(rules);

		EasyMock.verify(cache);
	}

}
