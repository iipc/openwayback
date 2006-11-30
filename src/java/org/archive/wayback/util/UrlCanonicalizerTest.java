/* UrlCanonicalizerTest
 *
 * $Id$
 *
 * Created on 2:13:36 PM Oct 11, 2006.
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
package org.archive.wayback.util;

import org.apache.commons.httpclient.URIException;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlCanonicalizerTest extends TestCase {
	private UrlCanonicalizer canonicalizer = new UrlCanonicalizer();
	/**
	 * Test method for 'org.archive.wayback.cdx.CDXRecord.urlStringToKey(String)'
	 */
	public void testUrlStringToKey() {

		checkAuthority("foo.com",true);
		checkAuthority("foo.con",false);
		checkAuthority("foo.de",true);
		checkAuthority("foo.denny",false);
		checkAuthority("1.1.1.1",true);
		checkAuthority("23.4.4.foo",false);
		checkAuthority("23.4.4.com",true);
		checkAuthority("com.23.4.4.134",false);
		
		
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
		checkCanonicalization("foo.com/pa%20th","foo.com/pa+th");
		
		// replace escaped ' ' with '+' in path plus kill trailing slash
//		checkCanonicalization("foo.com/pa%20th/","foo.com/pa+th");

		// replace multiple consecutive /'s in path
		checkCanonicalization("foo.com//goo","foo.com/goo");

		// replace multiple consecutive /'s in path
		checkCanonicalization("foo.com///goo","foo.com/goo");

		// leave alone consecutive /'s after ?
		checkCanonicalization("foo.com/b?jar=//goo","foo.com/b?jar=//goo");

		// replace multiple consecutive /'s in path, plus kill trailing /
//		checkCanonicalization("foo.com///goo/","foo.com/goo");

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

		// strip jsessionid
		String sid1 = "jsessionid=0123456789abcdefghijklemopqrstuv";
		String sid2 = "PHPSESSID=9682993c8daa2c5497996114facdc805";
		String sid3 = "sid=9682993c8daa2c5497996114facdc805";
		String sid4 = "ASPSESSIONIDAQBSDSRT=EOHBLBDDPFCLHKPGGKLILNAM";
		String sid5 = "CFID=12412453&CFTOKEN=15501799";
		//String sid6 = "CFID=3304324&CFTOKEN=57491900&jsessionid=a63098d96360$B0$D9$A";

		String fore = "http://foo.com/bar?bo=lo&";
		String aft = "&gum=yum";
		String want = "foo.com/bar?bo=lo&gum=yum";
//		String fore = "http://www.archive.org/index.html?";
//		String aft = "";
//		String want = "archive.org/index.html";
		
		checkCanonicalization(fore + sid1 + aft,want);
		checkCanonicalization(fore + sid2 + aft,want);
		checkCanonicalization(fore + sid3 + aft,want);
		checkCanonicalization(fore + sid4 + aft,want);
		checkCanonicalization(fore + sid5 + aft,want);
		//checkCanonicalization(fore + sid6 + aft,want);
		
		// strip port 80
		checkCanonicalization("http://www.chub.org:80/foo","chub.org/foo");

		// but not other ports...
		checkCanonicalization("http://www.chub.org:8080/foo","chub.org:8080/foo");

	}
	
	private void checkAuthority(String s, boolean want) {
		boolean got = canonicalizer.isAuthority(s);
		if(want) {
			assertTrue("String("+s+") could be an Authority",want == got);
		} else {
			assertTrue("String("+s+") is not an Authority",want == got);	
		}
	}
	
	private void checkCanonicalization(String orig, String want) {
		String got;
		try {
			got = canonicalizer.urlStringToKey(orig);
			assertEquals("Failed canonicalization (" + orig + ") => (" + got + 
					") and not (" + want + ") as expected",want,got);
			
			String got2 = canonicalizer.urlStringToKey(got);
			assertEquals("Failed 2nd canonicalization (" + got + ") => (" + 
					got2 + ") and not (" + want + ") as expected",want,got2);
			
			
		} catch (URIException e) {
			e.printStackTrace();
			assertTrue("Exception converting(" + orig + ")",false);
		}
	}
}
