/* ArchivalUrlTest
 *
 * $Id$:
 *
 * Created on Jun 4, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
	
	public void trt(String want, String src) throws BetterRequestException, BadQueryException {
		assertEquals(want,parseAU(src).toString());
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
