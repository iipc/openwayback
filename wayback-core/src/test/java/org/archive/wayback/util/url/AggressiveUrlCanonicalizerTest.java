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
package org.archive.wayback.util.url;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AggressiveUrlCanonicalizerTest extends TestCase {
	private AggressiveUrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
	/**
	 * Test method for 'org.archive.wayback.cdx.CDXRecord.urlStringToKey(String)'
	 */
	public void testUrlStringToKey() {

		// simple strip of http://
		checkCanonicalization("http://foo.com/","foo.com/");

		// simple strip of https://
		checkCanonicalization("https://foo.com/","foo.com/");

		// simple strip of ftp://
		checkCanonicalization("ftp://foo.com/","foo.com/");

		// simple strip of rtsp://
		checkCanonicalization("rtsp://foo.com/","foo.com/");

		// strip leading 'www.'
		checkCanonicalization("http://www.foo.com/","foo.com/");
		
		// add trailing '/' with empty path
		checkCanonicalization("http://www.foo.com","foo.com/");
		
		// strip leading 'www##.'
		checkCanonicalization("http://www12.foo.com/","foo.com/");

		// strip leading 'www##.' with https
		checkCanonicalization("https://www12.foo.com/","foo.com/");
		
		// strip leading 'www##.' with no protocol
		checkCanonicalization("www12.foo.com/","foo.com/");
		
		checkCanonicalization("http://www.example.com/","example.com/");
		checkCanonicalization("http://www.example.com","example.com/");
		checkCanonicalization("http://www.example.com/index.html","example.com/index.html");

		// leave alone an url with no protocol but non-empty path
		checkCanonicalization("foo.com/","foo.com/");
		
		// add trailing '/' with empty path and without protocol
		checkCanonicalization("foo.com","foo.com/");

		// add trailing '/' to with empty path and no protocol, plus massage
		checkCanonicalization("www12.foo.com","foo.com/");

		// do not add trailing '/' non-empty path and without protocol
		checkCanonicalization("foo.com/boo","foo.com/boo");

		// TEST
		// replace escaped ' ' with '+' in path plus keep trailing slash and query
		checkCanonicalization("foo.com/pa%20th?a=b","foo.com/pa+th?a=b");
		
		
		// replace escaped ' ' with '+' in path
		checkCanonicalization("foo.com/pa%20th","foo.com/pa+th");
		
		// replace escaped ' ' with '+' in path plus leave trailing slash
		checkCanonicalization("foo.com/pa%20th/","foo.com/pa+th/");

		// replace multiple consecutive /'s in path
		checkCanonicalization("foo.com//goo","foo.com/goo");

		// replace multiple consecutive /'s in path
		checkCanonicalization("foo.com///goo","foo.com/goo");

		// leave alone consecutive /'s after ?
		checkCanonicalization("foo.com/b?jar=//goo","foo.com/b?jar=//goo");

		// replace multiple consecutive /'s in path, plus leave trailing /
		checkCanonicalization("foo.com///goo/","foo.com/goo/");

		// replace escaped ' ' with '+' in path plus keep trailing slash and query
		checkCanonicalization("foo.com/pa%20th/?a=b","foo.com/pa+th/?a=b");
		
		
		// replace escaped ' ' with '+' in path but not in query key
		checkCanonicalization("foo.com/pa%20th?a%20a=b","foo.com/pa+th?a%20a=b");

		// replace escaped ' ' with '+' in path but not in query value
		checkCanonicalization("foo.com/pa%20th?a=b%20b","foo.com/pa+th?a=b%20b");

		
		// no change in '!' escaping
		checkCanonicalization("foo.com/pa!th","foo.com/pa!th");

		// no change in '+' escaping
		checkCanonicalization("foo.com/pa+th","foo.com/pa+th");

		// unescape legal escaped '!' (%21)
		checkCanonicalization("foo.com/pa%21th","foo.com/pa!th");

		// leave '%' (%25)
		checkCanonicalization("foo.com/pa%th","foo.com/pa%th");

		// unescape '%' (%25)
		checkCanonicalization("foo.com/pa%25th","foo.com/pa%th");
		
		//"http://wayback.archive-it.org/1726/20091231154920cs_/http://alumni.creighton.edu/atf/cf/%257B82F49357-B0BC-48DA-B47F-5701CAC6EDFE%257D/MENU-CSSPLAY.css"
		checkCanonicalization("foo.com/{a}b","foo.com/%7Ba%7Db");
		checkCanonicalization("foo.com/%7Ba%7Db","foo.com/%7Ba%7Db");
		
		
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
		String sid6 = "CFID=3304324&CFTOKEN=57491900&jsessionid=a63098d96360$B0$D9$A";

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
		checkCanonicalization(fore + sid6 + aft,want);

		// Check ASP_SESSIONID2:
		checkCanonicalization(
				"http://legislature.mi.gov/(S(4hqa0555fwsecu455xqckv45))/mileg.aspx",
				"legislature.mi.gov/mileg.aspx");

		// Check ASP_SESSIONID2 (again):
		checkCanonicalization(
				"http://legislature.mi.gov/(4hqa0555fwsecu455xqckv45)/mileg.aspx",
				"legislature.mi.gov/mileg.aspx");

		// Check ASP_SESSIONID3:
		checkCanonicalization(
				"http://legislature.mi.gov/(a(4hqa0555fwsecu455xqckv45)S(4hqa0555fwsecu455xqckv45)f(4hqa0555fwsecu455xqckv45))/mileg.aspx?page=sessionschedules",
				"legislature.mi.gov/mileg.aspx?page=sessionschedules");

		// '@' in path:
		checkCanonicalization(
				"http://www.flickr.com/photos/36050182@N05/",
				"flickr.com/photos/36050182@n05/");



		// default port stripping:
		
		// FIRST the easy-on-the-eyes

		// strip port 80
		checkCanonicalization("http://www.chub.org:80/foo","chub.org/foo");

		// but not other ports...
		checkCanonicalization("http://www.chub.org:8080/foo","chub.org:8080/foo");
		
		// but not other ports... with "www#." massage
		checkCanonicalization("http://www232.chub.org:8080/foo","chub.org:8080/foo");

		// default HTTP (:80) stripping without a scheme:
		checkCanonicalization("www.chub.org:80/foo","chub.org/foo");
		
		// no strip https port (443) without scheme:
		checkCanonicalization("www.chub.org:443/foo","chub.org:443/foo");

		// yes strip https port (443) with scheme:
		checkCanonicalization("https://www.chub.org:443/foo","chub.org/foo");
		
		// NEXT the exhaustive:
		String origHost = "www.chub.org";
		String massagedHost = "chub.org";
		String path = "/foo";
		for(String scheme : UrlOperations.ALL_SCHEMES) {

			int defaultPort = UrlOperations.schemeToDefaultPort(scheme);
			int nonDefaultPort = 19991;

			String origDefault = scheme + origHost + ":" + defaultPort + path;
			String canonDefault = massagedHost + path;

			String origNonDefault = 
				scheme + origHost + ":" + nonDefaultPort + path;
			String canonNonDefault =
				massagedHost + ":" + nonDefaultPort + path;

			checkCanonicalization(origDefault,canonDefault);
			checkCanonicalization(origNonDefault,canonNonDefault);
		}

		// should we try to pass all of these, too?
		// found in section 6.1 of:
		// http://code.google.com/p/google-safe-browsing/wiki/Protocolv2Spec

//		Canonicalize("http://host/%25%32%35") = "http://host/%25";
//		Canonicalize("http://host/%25%32%35%25%32%35") = "http://host/%25%25";
//		Canonicalize("http://host/%2525252525252525") = "http://host/%25";
//		Canonicalize("http://host/asdf%25%32%35asd") = "http://host/asdf%25asd";
//		Canonicalize("http://host/%%%25%32%35asd%%") = "http://host/%25%25%25asd%25%25";
//		Canonicalize("http://www.google.com/") = "http://www.google.com/";
//		Canonicalize("http://%31%36%38%2e%31%38%38%2e%39%39%2e%32%36/%2E%73%65%63%75%72%65/%77%77%77%2E%65%62%61%79%2E%63%6F%6D/") = "http://168.188.99.26/.secure/www.ebay.com/";
//		Canonicalize("http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/") = "http://195.127.0.11/uploads/%20%20%20%20/.verify/.eBaysecure=updateuserdataxplimnbqmn-xplmvalidateinfoswqpcmlx=hgplmcx/";  
//		Canonicalize("http://host%23.com/%257Ea%2521b%2540c%2523d%2524e%25f%255E00%252611%252A22%252833%252944_55%252B") = "http://host%23.com/~a!b@c%23d$e%25f^00&11*22(33)44_55+";
//		Canonicalize("http://3279880203/blah") = "http://195.127.0.11/blah";
//		Canonicalize("http://www.google.com/blah/..") = "http://www.google.com/";
//		Canonicalize("www.google.com/") = "http://www.google.com/";
//		Canonicalize("www.google.com") = "http://www.google.com/";
//		Canonicalize("http://www.evil.com/blah#frag") = "http://www.evil.com/blah";
//		Canonicalize("http://www.GOOgle.com/") = "http://www.google.com/";
//		Canonicalize("http://www.google.com.../") = "http://www.google.com/";
//		Canonicalize("http://www.google.com/foo\tbar\rbaz\n2") ="http://www.google.com/foobarbaz2";
//		Canonicalize("http://www.google.com/q?") = "http://www.google.com/q?";
//		Canonicalize("http://www.google.com/q?r?") = "http://www.google.com/q?r?";
//		Canonicalize("http://www.google.com/q?r?s") = "http://www.google.com/q?r?s";
//		Canonicalize("http://evil.com/foo#bar#baz") = "http://evil.com/foo";
//		Canonicalize("http://evil.com/foo;") = "http://evil.com/foo;";
//		Canonicalize("http://evil.com/foo?bar;") = "http://evil.com/foo?bar;";
//		Canonicalize("http://\x01\x80.com/") = "http://%01%80.com/";
//		Canonicalize("http://notrailingslash.com") = "http://notrailingslash.com/";
//		Canonicalize("http://www.gotaport.com:1234/") = "http://www.gotaport.com:1234/";
//		Canonicalize("  http://www.google.com/  ") = "http://www.google.com/";
//		Canonicalize("http:// leadingspace.com/") = "http://%20leadingspace.com/";
//		Canonicalize("http://%20leadingspace.com/") = "http://%20leadingspace.com/";
//		Canonicalize("%20leadingspace.com/") = "http://%20leadingspace.com/";
//		Canonicalize("https://www.securesite.com/") = "https://www.securesite.com/";
//		Canonicalize("http://host.com/ab%23cd") = "http://host.com/ab%23cd";
//		Canonicalize("http://host.com//twoslashes?more//slashes") = "http://host.com/twoslashes?more//slashes";
		
		
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
