/* CDXRecordTest
 *
 * $Id$
 *
 * Created on 3:08:30 PM Feb 23, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.cdx;

import org.apache.commons.httpclient.URIException;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXRecordTest extends TestCase {

	/**
	 * Test method for 'org.archive.wayback.cdx.CDXRecord.urlStringToKey(String)'
	 */
	public void testUrlStringToKey() {

		// simple strip of http://
		checkCanonicalization("http://foo.com/","foo.com/");

// would be nice to handle other protocols...
//		// simple strip of https://
//		checkCanonicalization("https://foo.com/","foo.com/");
//
//		// simple strip of ftp://
//		checkCanonicalization("ftp://foo.com/","foo.com/");
//
//		// simple strip of rtsp://
//		checkCanonicalization("rtsp://foo.com/","foo.com/");

		// strip leading 'www.'
		checkCanonicalization("http://www.foo.com/","foo.com/");
		
		// add trailing '/' with empty path
		checkCanonicalization("http://www.foo.com","foo.com/");
		
		// strip leading 'www##.'
		checkCanonicalization("http://www12.foo.com/","foo.com/");
		
		// strip leading 'www##.' with no protocol
		checkCanonicalization("www12.foo.com/","foo.com/");
		
		
		// leave alone an url with no protocol but non-empty path
		checkCanonicalization("foo.com/","foo.com/");
		
		// add trailing '/' with empty path and without protocol
		checkCanonicalization("foo.com","foo.com/");

		// add trailing '/' to with empty path and no protocol, plus massage
		checkCanonicalization("www12.foo.com","foo.com/");

		// do not add trailing '/' non-empty path and without protocol
		checkCanonicalization("foo.com/boo","foo.com/boo");
		
		// replace escaped ' ' with '+' in path
		checkCanonicalization("foo.com/pa%20th/","foo.com/pa+th/");
		
		// replace escaped ' ' with '+' in path plus keep trailing slash
		checkCanonicalization("foo.com/pa%20th","foo.com/pa+th");

		// replace escaped ' ' with '+' in path plus keep trailing slash and query
		checkCanonicalization("foo.com/pa%20th?a=b","foo.com/pa+th?a=b");
		
		
		// replace escaped ' ' with '+' in path but not in query key
		checkCanonicalization("foo.com/pa%20th?a%20a=b","foo.com/pa+th?a%20a=b");

		// replace escaped ' ' with '+' in path but not in query value
		checkCanonicalization("foo.com/pa%20th?a=b%20b","foo.com/pa+th?a=b%20b");

		// replace escaped ' ' with '+' in path, unescape legal '!' in path
		// no change in query escaping
		checkCanonicalization("foo.com/pa%20t%21h?a%20a=b","foo.com/pa+t!h?a%20a=b");
		
		// replace escaped ' ' with '+' in path, leave illegal '%02' in path
		// no change in query escaping
		checkCanonicalization("foo.com/pa%20t%02h?a%20a=b","foo.com/pa+t%02h?a%20a=b");

	}
	private void checkCanonicalization(String orig, String want) {
		String got;
		try {
			got = CDXRecord.urlStringToKey(orig);
			assertEquals("Failed canonicalization (" + orig + ") => (" + got + 
					") and not (" + want + ") as expected",got,want);
		} catch (URIException e) {
			e.printStackTrace();
			assertTrue("Exception converting(" + orig + ")",false);
		}
	}

}
