/* ReplayRequestParserTest
 *
 * $Id$
 *
 * Created on 12:03:48 PM Feb 12, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl.requestparser;

import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.BaseRequestParser;

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
	 */
	public void testParseString() {
		BaseRequestParser wrapped = new ArchivalUrlRequestParser();
		ReplayRequestParser p = new ReplayRequestParser(wrapped);
		WaybackRequest r;
		r = p.parse("");
		assertNull("Should not parse empty string", r);
		r = p.parse("20070101000000/foo.com");
		assertNotNull("Should parse legit request sans scheme", r);
		assertEquals("parsed request Url",r.getRequestUrl(),"http://foo.com");
		assertEquals("Parsed timestamp","20070101000000",r.getReplayTimestamp());

		r = p.parse("20070101000000/foo.com/");
		assertEquals("parsed request Url, maintaining trailing slash",
				"http://foo.com/",r.getRequestUrl());

		r = p.parse("200701010000/foo.com");
		assertEquals("parsed partial date",
				"http://foo.com",r.getRequestUrl());
		assertEquals("Parsed partial timestamp to earliest",
				"20070101000000",r.getReplayTimestamp());

		r = p.parse("20070101000000/http://foo.com");
		assertEquals("parsed request Url with scheme",
				"http://foo.com",r.getRequestUrl());

		r = p.parse("20070101000000/http://foo.com/");
		assertEquals("parsed request Url with scheme and trailing slash",
				"http://foo.com/",r.getRequestUrl());

		r = p.parse("20070101000000/ftp://foo.com/");
		assertEquals("parsed request Url with ftp scheme",
				"ftp://foo.com/",r.getRequestUrl());
		
		r = p.parse("20070101000000/https://foo.com/");
		assertEquals("parsed request Url with https scheme",
				"https://foo.com/",r.getRequestUrl());

		r = p.parse("20070101000000js_/http://foo.com/");
		assertEquals("parsed request Url with js_ flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed js_ flag",r.isJSContext());
		assertFalse("css not set",r.isCSSContext());

		r = p.parse("20070101000000cs_/http://foo.com/");
		assertEquals("parsed request Url with cs_ flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertFalse("js not set",r.isJSContext());

		r = p.parse("20070101000000cs_js_/http://foo.com/");
		assertEquals("parsed request Url with cs_ and js_ flags",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000js_cs_/http://foo.com/");
		assertEquals("parsed request Url with cs_ and js_ flags, backvards",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_/http://foo.com/");
		assertEquals("parsed request Url with unknown flag",
				"http://foo.com/",r.getRequestUrl());
		assertFalse("no cs_ flag",r.isCSSContext());
		assertFalse("no js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_js_cs_/http://foo.com/");
		assertEquals("parsed request Url with falgs and unknown flag",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000js_cs_un_/http://foo.com/");
		assertEquals("parsed request Url with falgs and unknown flag at end",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

		r = p.parse("20070101000000un_js_cs_un_/http://foo.com/");
		assertEquals("parsed request Url with falgs and unknown flags",
				"http://foo.com/",r.getRequestUrl());
		assertTrue("parsed cs_ flag",r.isCSSContext());
		assertTrue("parsed js_ flag",r.isJSContext());

	}
}
