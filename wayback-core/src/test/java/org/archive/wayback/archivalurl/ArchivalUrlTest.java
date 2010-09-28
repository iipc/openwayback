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
package org.archive.wayback.archivalurl;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.webapp.AccessPoint;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ArchivalUrlTest extends TestCase {
	ArchivalUrlRequestParser parser = new ArchivalUrlRequestParser();
	WaybackRequest wbr;
	ArchivalUrl au;
	BaseRequestParser brp = new BaseRequestParser() {
		public int getMaxRecords() { return 10;	}
		@Override
		public WaybackRequest parse(HttpServletRequest httpRequest,
				AccessPoint wbContext) throws BadQueryException,
				BetterRequestException {
			return null;
		}
	};
	PathRequestParser parsers[] = new PathRequestParser[] {
			new ReplayRequestParser(brp),
			new PathDatePrefixQueryRequestParser(brp),
			new PathDateRangeQueryRequestParser(brp),
			new PathPrefixDatePrefixQueryRequestParser(brp),
			new PathPrefixDateRangeQueryRequestParser(brp),
	};

	private WaybackRequest parse(String path) 
	throws BetterRequestException, BadQueryException {

		WaybackRequest wbRequest = null;

		for(int i = 0; i < parsers.length; i++) {
			wbRequest = parsers[i].parse(path, null);
			if(wbRequest != null) {
				break;
			}
		}
		return wbRequest;
	}
	private ArchivalUrl parseAU(String path) 
	throws BetterRequestException, BadQueryException {
		return new ArchivalUrl(parse(path));
	}
	
	private void trt(String want, String src) {
		try {
			assertEquals(want,parseAU(src).toString());
		} catch (BetterRequestException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (BadQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private void trtBetterExcept(String want, String src) {
		try {
			String foo = parseAU(src).toString();
			fail("should have thrown BetterRequestException");
		} catch (BetterRequestException e) {
			assertEquals(want,e.getBetterURI());
		} catch (BadQueryException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toString()}.
	 * @throws BadQueryException 
	 * @throws BetterRequestException 
	 */
	public void testToString() throws BetterRequestException, BadQueryException {
		trt(
				"20010101000000/http://yahoo.com/",
				"20010101000000/http://yahoo.com/");
		
		trt(
				"20010101000000/http://yahoo.com/",
				"20010101000000/http://yahoo.com:80/");
		
		trt(
				"20010101000000/http://www.yahoo.com/",
				"20010101000000/http://www.yahoo.com:80/");
		trt(
				"20010101000000/http://www.yahoo.com/",
				"20010101000000/www.yahoo.com/");
		trt(
				"20010101000000/http://www.yahoo.com/",
				"20010101000000/www.yahoo.com:80/");
		
		trt(
				"20010101000000im_/http://www.yahoo.com/",
				"20010101000000im_/www.yahoo.com:80/");

		trt(
				"20010101235959im_/http://www.yahoo.com/",
				"20010101im_/www.yahoo.com:80/");
	}

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toPrefixQueryString(java.lang.String)}.
	 */
	public void testToPrefixQueryString() {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setUrlQueryRequest();
		wbr.setRequestUrl("http://www.yahoo.com/");
		ArchivalUrl au = new ArchivalUrl(wbr);
		
		assertEquals("*/http://www.yahoo.com/*",au.toString());
	}

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toQueryString(java.lang.String)}.
	 */
	public void testToQueryString() {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setCaptureQueryRequest();
		wbr.setRequestUrl("http://www.yahoo.com/");
		ArchivalUrl au = new ArchivalUrl(wbr);
		assertEquals("*/http://www.yahoo.com/",au.toString());
	}

	/**
	 * Test method for {@link org.archive.wayback.archivalurl.ArchivalUrl#toReplayString(java.lang.String)}.
	 */
	public void testToReplayString() {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setReplayRequest();
		wbr.setReplayTimestamp("20010101000000");
		wbr.setRequestUrl("http://www.yahoo.com/");
		ArchivalUrl au = new ArchivalUrl(wbr);
		assertEquals("20010101000000/http://www.yahoo.com/",au.toString());
	}
}
