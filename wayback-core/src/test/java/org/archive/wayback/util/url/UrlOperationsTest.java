package org.archive.wayback.util.url;

import junit.framework.TestCase;

/**
 * Stub for testing UrlOperations static methods
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UrlOperationsTest extends TestCase {

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
	}
	private void assertSDP(String want, String orig) {
		String got = UrlOperations.stripDefaultPortFromUrl(orig);
		assertEquals(want,got);
	}
	
}
