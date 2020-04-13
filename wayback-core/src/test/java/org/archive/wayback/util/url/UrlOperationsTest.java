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

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Stub for testing UrlOperations static methods
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlOperationsTest extends TestCase {

	public void testOneSlashUrl() throws MalformedURLException {
		assertEquals("http://one.com/",
				UrlOperations.fixupHTTPUrlWithOneSlash("http://one.com/"));
		assertEquals("http://one.com",
				UrlOperations.fixupHTTPUrlWithOneSlash("http://one.com"));
		assertEquals("http://http://one.com",
				UrlOperations.fixupHTTPUrlWithOneSlash("http://http://one.com"));
		assertEquals("http://one.com",
				UrlOperations.fixupHTTPUrlWithOneSlash("http:/one.com"));
		assertEquals("http://one.com/",
				UrlOperations.fixupHTTPUrlWithOneSlash("http:/one.com/"));
		assertEquals("http://one.com/foo.html",
				UrlOperations.fixupHTTPUrlWithOneSlash("http:/one.com/foo.html"));

	}
	
	public void testFixupScheme() {
		assertEquals("http://one.com/foo.html",
			UrlOperations.fixupScheme("http:/one.com/foo.html"));
		assertEquals("https://one.com/foo.html",
			UrlOperations.fixupScheme("https:/one.com/foo.html"));
		assertEquals("ftp://one.com/foo.html",
			UrlOperations.fixupScheme("ftp:/one.com/foo.html"));
		assertEquals("rtsp://one.com/foo.html",
			UrlOperations.fixupScheme("rtsp:/one.com/foo.html"));
		assertEquals("mms://one.com/foo.html",
			UrlOperations.fixupScheme("mms:/one.com/foo.html"));

		assertEquals(
			"http://web.archive.org/web/2010/http:/example.com",
			UrlOperations
				.fixupScheme("http://web.archive.org/web/2010/http:/example.com"));

		final String url = "http://example.com/well/formed.html";
		assertTrue(url == UrlOperations.fixupScheme(url));
	}
	
	/**
	 * Test of {@link UrlOperations#fixupScheme(String, String)}
	 */
	public void testFixupScheme2() {
		assertEquals("http://one.com/foo.html", UrlOperations.fixupScheme("one.com/foo.html", "http://"));
		assertEquals("http://one.com:80/foo.html", UrlOperations.fixupScheme("one.com:80/foo.html", "http://"));
		assertEquals("https://one.com/foo.html", UrlOperations.fixupScheme("one.com/foo.html", "https://"));
		assertEquals("ftp://one.com/foo.html", UrlOperations.fixupScheme("ftp://one.com/foo.html", "http://"));
		assertEquals("one.com/foo.html", UrlOperations.fixupScheme("one.com/foo.html", null));
		assertEquals("http://one.com/foo.html", UrlOperations.fixupScheme("http:/one.com/foo.html", null));
		assertEquals("http://one.com/foo.html", UrlOperations.fixupScheme("http:/one.com/foo.html", "http://"));
		assertEquals("http://one.com:80/foo.html", UrlOperations.fixupScheme("http:/one.com:80/foo.html", "http://"));
		assertEquals("youtube-dl:https://example.com/", UrlOperations.fixupScheme("youtube-dl:https://example.com/", "http://"));
		assertEquals("youtube-dl:https://example.com/", UrlOperations.fixupScheme("youtube-dl:https://example.com/", null));
		assertEquals("youtube-dl:https://example.com/", UrlOperations.fixupScheme("youtube-dl:https:/example.com/", null));
		assertEquals("screenshot:https://example.com/", UrlOperations.fixupScheme("screenshot:https://example.com/", "http://"));
		assertEquals("screenshot:https://example.com/", UrlOperations.fixupScheme("screenshot:https://example.com/", null));
		assertEquals("screenshot:https://example.com/", UrlOperations.fixupScheme("screenshot:https:/example.com/", null));
		assertEquals("thumbnail:https://example.com/", UrlOperations.fixupScheme("thumbnail:https://example.com/", "http://"));
		assertEquals("thumbnail:https://example.com/", UrlOperations.fixupScheme("thumbnail:https://example.com/", null));
		assertEquals("thumbnail:https://example.com/", UrlOperations.fixupScheme("thumbnail:https:/example.com/", null));
		assertEquals("urn:transclusions:https://example.com/", UrlOperations.fixupScheme("urn:transclusions:https://example.com/", "http://"));
		assertEquals("urn:transclusions:https://example.com/", UrlOperations.fixupScheme("urn:transclusions:https://example.com/", null));
		assertEquals("urn:transclusions:https://example.com/", UrlOperations.fixupScheme("urn:transclusions:https:/example.com/", null));
		assertEquals("youtube-dl:00001:https://example.com/", UrlOperations.fixupScheme("youtube-dl:00001:https://example.com/", "http://"));
		assertEquals("youtube-dl:00001:https://example.com/", UrlOperations.fixupScheme("youtube-dl:00001:https://example.com/", null));
		assertEquals("youtube-dl:00001:https://example.com/", UrlOperations.fixupScheme("youtube-dl:00001:https:/example.com/", null));
		assertEquals("http://foo.com:80/blah", UrlOperations.fixupScheme("foo.com:80/blah", "http://"));
		assertEquals("http://foo.com:80", UrlOperations.fixupScheme("http:/foo.com:80", "http://"));
		// XXX not currently supported
		// assertEquals("youtube-dl:foo.com/blah", UrlOperations.fixupScheme("youtube-dl:00001:foo.com:80/blah", "http://"));
		// assertEquals("youtube-dl:00001:foo.com:80/blah", UrlOperations.fixupScheme("youtube-dl:00001:foo.com:80/blah", "http://"));
		// assertEquals("youtube-dl:00001:foo.com:80", UrlOperations.fixupScheme("youtube-dl:00001:foo.com:80", "http://"));

	}

	public void testIsAuthority() {
		checkAuthority("foo.com",true);
		checkAuthority("foo.con",false);
		checkAuthority("foo.de",true);
		checkAuthority("foo.denny",false);
		checkAuthority("1.1.1.1",true);
		checkAuthority("23.4.4.foo",false);
		checkAuthority("23.4.4.com",true);
		checkAuthority("com.23.4.4.134",false);
	}
	
	private void checkAuthority(String s, boolean want) {
		boolean got = UrlOperations.isAuthority(s);
		if(want) {
			assertTrue("String("+s+") could be an Authority",want == got);
		} else {
			assertTrue("String("+s+") is not an Authority",want == got);	
		}
	}
	public void testUrlToHost() {
		assertEquals("foo.com",UrlOperations.urlToHost("dns:foo.com"));
		
		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com"));

		assertEquals("www.google.com",UrlOperations.urlToHost("http://www.GOOGLE.COM"));
		assertEquals("google.com",UrlOperations.urlToHost("http://GOOGLE.COM/"));
		assertEquals("google.com",UrlOperations.urlToHost("http://GOOGLE.COM"));
		assertEquals("google.com",UrlOperations.urlToHost("http://GOOGLE.COM:80"));
		assertEquals("google.com",UrlOperations.urlToHost("http://GOOGLE.COM:80/"));
		assertEquals("google.com",UrlOperations.urlToHost("http://GOOGLE.COM:80/foo"));

		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com/"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com/"));
		
		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com:120/"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com:180/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com:190/"));

		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com:120"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com:180"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com:190"));

		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com:120/path"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com:180/path"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com:190/path"));

		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com:120/path/"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com:180/path/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com:190/path/"));

		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com:120/path:/"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com:180/path:/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com:190/path:/"));
		
		assertEquals("foo.com",UrlOperations.urlToHost("http://foo.com/path:/"));
		assertEquals("foo.com",UrlOperations.urlToHost("https://foo.com/path:/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com/path:/"));
		assertEquals("foo.com",UrlOperations.urlToHost("ftp://foo.com\\"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://www.foo.com\\"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://www.foo.com:80\\"));

		
		assertEquals("foo.com",UrlOperations.urlToHost("http://user@foo.com"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user@www.foo.com"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user:pass@www.foo.com"));

		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user:pass@www.foo.com/"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user:pass@www.foo.com/boo@foo"));
	}
	public void testUrlToUserInfo() {
		assertEquals(null,UrlOperations.urlToUserInfo("dns:foo.com"));
		assertEquals(null,UrlOperations.urlToUserInfo("http://foo.com"));
		assertEquals(null,UrlOperations.urlToUserInfo("https://foo.com"));
		assertEquals(null,UrlOperations.urlToUserInfo("ftp://foo.com"));
		assertEquals(null,UrlOperations.urlToUserInfo("ftp://foo.com/"));
		assertEquals(null,UrlOperations.urlToUserInfo("http://foo.com:80/"));
		assertEquals(null,UrlOperations.urlToUserInfo("http://foo.com:80"));
		assertEquals(null,UrlOperations.urlToUserInfo("http://www.foo.com:80\\"));
		assertEquals(null,UrlOperations.urlToUserInfo("http://www.flickr.com/photos/36050182@N05/"));
		
		
		assertEquals("user",UrlOperations.urlToUserInfo("http://user@foo.com"));
		assertEquals("user",UrlOperations.urlToUserInfo("http://user@www.foo.com"));
		assertEquals("user:pass",UrlOperations.urlToUserInfo("http://user:pass@www.foo.com"));
		assertEquals("user:pass",UrlOperations.urlToUserInfo("http://user:pass@www.foo.com:8080"));
		assertEquals("user:pass",UrlOperations.urlToUserInfo("http://user:pass@www.foo.com:8080/boo@arb"));

		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user:pass@www.foo.com/"));
		assertEquals("www.foo.com",UrlOperations.urlToHost("http://user:pass@www.foo.com/boo@foo"));
	}
	public void testResolveUrl() {
		for(String scheme : UrlOperations.ALL_SCHEMES) {

			assertEquals(scheme + "a.org/1/2",
				UrlOperations.resolveUrl(scheme + "a.org/3/","/1/2"));

			assertEquals(scheme + "b.org/1/2",
				UrlOperations.resolveUrl(scheme + "a.org/3/",
						scheme + "b.org/1/2"));

			assertEquals(scheme + "a.org/3/1/2",
				UrlOperations.resolveUrl(scheme + "a.org/3/","1/2"));

			assertEquals(scheme + "a.org/1/2",
					UrlOperations.resolveUrl(scheme + "a.org/3","1/2"));
			assertEquals(scheme + "a.org/3",
					UrlOperations.resolveUrl(scheme + "a.org/3",""));
			assertEquals(scheme + "a.org/3.html",
					UrlOperations.resolveUrl(scheme + "a.org/3.html",""));
		}
	}
	public void testUrlToScheme() {
		assertEquals("http://",UrlOperations.urlToScheme("http://a.com/"));
		assertEquals("https://",UrlOperations.urlToScheme("https://a.com/"));
		assertEquals("ftp://",UrlOperations.urlToScheme("ftp://a.com/"));
		assertEquals("rtsp://",UrlOperations.urlToScheme("rtsp://a.com/"));
		assertEquals("mms://",UrlOperations.urlToScheme("mms://a.com/"));
		assertNull(UrlOperations.urlToScheme("blah://a.com/"));
	}
	
	public void testGetUrlParentDir() {

		assertEquals(                         "http://a.b/c/",
				UrlOperations.getUrlParentDir("http://a.b/c/d"));

		assertEquals(                         "http://a.b/",
				UrlOperations.getUrlParentDir("http://a.b/c/"));

		assertEquals(                         "http://a.b/",
				UrlOperations.getUrlParentDir("http://a.b/c"));

		assertEquals(                         "http://a.b/c/d/e/",
				UrlOperations.getUrlParentDir("http://a.b/c/d/e/f"));

		assertEquals(                         "http://a.b/",
				UrlOperations.getUrlParentDir("http://a.b/c?d=e"));

		assertEquals(                         null,
				UrlOperations.getUrlParentDir("http://a.b/"));
		
		assertEquals(                         null,
				UrlOperations.getUrlParentDir("http//a.b/"));

		assertEquals(                         null,
				UrlOperations.getUrlParentDir("http://"));

		assertEquals(                         null,
				UrlOperations.getUrlParentDir("http://#4.8gifdijdf"));

		assertEquals(                         null,
				UrlOperations.getUrlParentDir("http://#4.8gifdijdf/a/b"));
		
		
	}
	public void testUrlPath() {
		assertEquals("/",UrlOperations.getURLPath("http://foo.com"));
		assertEquals("/",UrlOperations.getURLPath("http://foo.com/"));
		assertEquals("/",UrlOperations.getURLPath("http://foo.com:80/"));
		assertEquals("/blue",UrlOperations.getURLPath("http://foo.com:80/blue"));
		assertEquals("/blue/red",UrlOperations.getURLPath("http://foo.com:80/blue/red"));
		assertEquals("/blue/red:colon",UrlOperations.getURLPath("http://foo.com:80/blue/red:colon"));

		assertEquals("/",UrlOperations.getURLPath("foo.com"));
		assertEquals("/",UrlOperations.getURLPath("foo.com:80"));
		assertEquals("/",UrlOperations.getURLPath("foo.com:8080"));
		assertEquals("/",UrlOperations.getURLPath("foo.com/"));
		assertEquals("/",UrlOperations.getURLPath("foo.com:80/"));
		assertEquals("/",UrlOperations.getURLPath("foo.com:8080/"));
		assertEquals("/bar",UrlOperations.getURLPath("foo.com/bar"));
		assertEquals("/bar",UrlOperations.getURLPath("foo.com:80/bar"));
		assertEquals("/bar",UrlOperations.getURLPath("foo.com:8080/bar"));

		assertEquals("/bar/baz",UrlOperations.getURLPath("foo.com/bar/baz"));
		assertEquals("/bar/baz",UrlOperations.getURLPath("foo.com:80/bar/baz"));
		assertEquals("/bar/baz",UrlOperations.getURLPath("foo.com:8080/bar/baz"));

	}
	public void testStripDefaultPort() {
		assertSDP("http://foo.com/","http://foo.com/");
		assertSDP("http://foo.com","http://foo.com");
		assertSDP("http://foo.com","http://foo.com:80");
		assertSDP("foo.com:80/","foo.com:80/");
		assertSDP("http://foo.com:8080/","http://foo.com:8080/");
		assertSDP("http://foo.com:8081/","http://foo.com:8081/");
		assertSDP("https://foo.com:8081/","https://foo.com:8081/");
		assertSDP("https://foo.com/","https://foo.com:443/");
		assertSDP("https://foo.com","https://foo.com:443");
		assertSDP("ftp://foo.com/","ftp://foo.com/");
		assertSDP("ftp://foo.com","ftp://foo.com");
		assertSDP("ftp://foo.com:1234","ftp://foo.com:1234");
		assertSDP("ftp://foo.com","ftp://foo.com:21");
		assertSDP("ftp://foo.com/","ftp://foo.com:21/");
		assertSDP("ftp://foo.com/bla","ftp://foo.com:21/bla");
		assertSDP("s3://foo.com/","s3://foo.com/");
		assertSDP("s3://foo.com/bar","s3://foo.com/bar");
		assertSDP("s3://foo.com:80/bar","s3://foo.com:80/bar");
		assertSDP("http://b@foo.com/bar","http://b@foo.com:80/bar");
		assertSDP("http://b@foo.com/bar","http://b@foo.com/bar");
		assertSDP("http://b:80@foo.com/bar","http://b:80@foo.com/bar");
		assertSDP("http://b:80@foo.com/bar","http://b:80@foo.com:80/bar");
		assertSDP("http://b:80@foo.com:8080/ba","http://b:80@foo.com:8080/ba");
		assertSDP("http://www.flickr.com/photos/36050182@N05/","http://www.flickr.com/photos/36050182@N05/");
		
	}
	private void assertSDP(String want, String orig) {
		String got = UrlOperations.stripDefaultPortFromUrl(orig);
		assertEquals(want,got);
	}
	
}
