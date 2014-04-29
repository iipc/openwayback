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
package org.archive.wayback.archivalurl.requestparser;

import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.webapp.AccessPoint;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ReplayRequestParserTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.requestparser.ReplayRequestParser#parse(java.lang.String)}.
	 * @throws BetterRequestException 
	 */
	public void testParseString() throws Exception {
		BaseRequestParser wrapped = new ArchivalUrlRequestParser();
		ReplayRequestParser p = new ReplayRequestParser(wrapped);
		WaybackRequest r;
		AccessPoint ap = null;
		r = p.parse("",ap);
		assertNull("Should not parse empty string", r);
		r = p.parse("20070101000000/foo.com",ap);
		assertNotNull("Should parse legit request sans scheme", r);
		assertEquals("parsed request Url",r.getRequestUrl(),"http://foo.com");
		assertEquals("Parsed timestamp","20070101000000",r.getReplayTimestamp());

		r = p.parse("20070101000000/foo.com/",ap);
		assertEquals("parsed request Url, maintaining trailing slash",
				"http://foo.com/",r.getRequestUrl());

		r = p.parse("200701010000/foo.com",ap);
		assertEquals("parsed partial date",
				"http://foo.com",r.getRequestUrl());
		assertEquals("Parsed partial timestamp to earliest",
				"20070101000000",r.getReplayTimestamp());

		r = p.parse("20070101000000/http://foo.com",ap);
		assertEquals("parsed request Url with scheme",
				"http://foo.com",r.getRequestUrl());

		r = p.parse("20070101000000/http://foo.com/",ap);
		assertEquals("parsed request Url with scheme and trailing slash",
				"http://foo.com/",r.getRequestUrl());

		r = p.parse("20070101000000/ftp://foo.com/",ap);
		assertEquals("parsed request Url with ftp scheme",
				"ftp://foo.com/",r.getRequestUrl());
		
		r = p.parse("20070101000000/https://foo.com/",ap);
		assertEquals("parsed request Url with https scheme",
				"https://foo.com/",r.getRequestUrl());

		r = p.parse("20070101000000js_/http://foo.com/",ap);
		assertEquals("parsed request Url with js_ flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed js_ flag",r.isJSContext());
		assertFalse("css not set",r.isCSSContext());

		r = p.parse("20070101000000cs_/http://foo.com/",ap);
		assertEquals("parsed request Url with cs_ flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertFalse("js not set",r.isJSContext());

		r = p.parse("20070101000000cs_js_/http://foo.com/",ap);
		assertEquals("parsed request Url with cs_ and js_ flags",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000js_cs_/http://foo.com/",ap);
		assertEquals("parsed request Url with cs_ and js_ flags, backvards",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_/http://foo.com/",ap);
		assertEquals("parsed request Url with unknown flag",
				"http://foo.com/",r.getRequestUrl());
		assertFalse("no cs_ flag",r.isCSSContext());
		assertFalse("no js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_js_cs_/http://foo.com/",ap);
		assertEquals("parsed request Url with falgs and unknown flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000js_cs_un_/http://foo.com/",ap);
		assertEquals("parsed request Url with falgs and unknown flag at end",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_js_cs_un_/http://foo.com/",ap);
		assertEquals("parsed request Url with falgs and unknown flags",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

	}
}
