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
}
