package org.archive.wayback.proxy;

import junit.framework.TestCase;

/**
 * test for {@link ProxyHttpsResultURIConverter}.
 * @author kenji
 *
 */
public class ProxyHttpsResultURIConverterTest extends TestCase {
	
	ProxyHttpsResultURIConverter cut;
	
	protected void setUp() throws Exception {
		cut = new ProxyHttpsResultURIConverter();
	}
	
	public void testMakeReplayURI() {
		final String input = "http://home.archive.org/index.html";
		assertEquals(input, cut.makeReplayURI("20140404102345", input));
	}
	
	public void testMakeReplayURI_https() {
		final String input = "https://home.archive.org/index.html";
		assertEquals(input.replaceFirst("https:", "http:"),
				cut.makeReplayURI("20140404102345", input));
	}
	
	public void testMakeReplayURI_justHostAndPath() {
		final String input = "home.archive.org/index.html";
		assertEquals("http://" + input, cut.makeReplayURI("20140404102345", input));
	}

	// followings test methods represent behavior of current implementation,
	// NOT an expected correct behavior.
	
	// unexpected input
	public void testMakeReplayURI_justPath() {
		final String input = "/index.html";
		assertEquals("http://" + input, cut.makeReplayURI("20140404102345", input));
	}
	// unexpected input
	public void testMakeReplayURI_relativePath() {
		final String input = "index.html";
		assertEquals("http://" + input, cut.makeReplayURI("20140404102345", input));
	}
	// unexpected input
	public void testMakeReplayURI_noScheme() {
		final String input = "//home.archive.org/";
		assertEquals("http://" + input, cut.makeReplayURI("20140404102345", input));
	}
	
}
