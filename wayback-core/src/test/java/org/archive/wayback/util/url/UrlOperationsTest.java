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
}
