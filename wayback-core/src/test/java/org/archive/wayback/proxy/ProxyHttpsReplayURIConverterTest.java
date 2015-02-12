package org.archive.wayback.proxy;

import junit.framework.TestCase;

import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.replay.ReplayContext;
import org.easymock.EasyMock;

/**
 * test for {@link ProxyHttpsReplayURIConverter}.
 * @author kenji
 *
 */
public class ProxyHttpsReplayURIConverterTest extends TestCase {

	ProxyHttpsReplayURIConverter cut;
	ReplayContext replayContext;

	protected void setUp() throws Exception {
		cut = new ProxyHttpsReplayURIConverter();
	}

	public void testMakeReplayURI() {
		final String input = "http://home.archive.org/index.html";
		assertEquals(input, cut.makeReplayURI("20140404102345", input, null, URLStyle.ABSOLUTE));
	}

	public void testMakeReplayURI_https() {
		final String input = "https://home.archive.org/index.html";
		assertEquals(input.replaceFirst("https:", "http:"),
			cut.makeReplayURI("20140404102345", input, null, URLStyle.ABSOLUTE));
	}
	
	public void testMakeReplayURI_https_rewriteHttpsOff() {
		cut.setRewriteHttps(false);
		final String input = "https://home.archive.org/index.html";
		assertEquals(input,
			cut.makeReplayURI("20140404102345", input, null, URLStyle.ABSOLUTE));
	}

	// Test of default ReplayURLTransformer impl

	public void testTranslate_justHostAndPath() {
		final String input = "home.archive.org/index.html";
		replayContext = EasyMock.createNiceMock(ReplayContext.class);
		EasyMock.expect(replayContext.getDatespec()).andStubReturn("20140404102345");

		assertEquals(input, cut.transform(replayContext, input, null));
	}

	public void testTranslate_justPath() {
		final String input = "/index.html";
		replayContext = EasyMock.createNiceMock(ReplayContext.class);
		EasyMock.expect(replayContext.getDatespec()).andStubReturn("20140404102345");

		assertEquals(input, cut.transform(replayContext, input, null));
	}

	public void testTranslate_relativePath() {
		final String input = "index.html";
		replayContext = EasyMock.createNiceMock(ReplayContext.class);
		EasyMock.expect(replayContext.getDatespec()).andStubReturn("20140404102345");

		assertEquals(input, cut.transform(replayContext, input, null));
	}

	public void testTranslate_noScheme() {
		final String input = "//home.archive.org/";
		replayContext = EasyMock.createNiceMock(ReplayContext.class);
		EasyMock.expect(replayContext.getDatespec()).andStubReturn("20140404102345");

		assertEquals(input, cut.transform(replayContext, input, null));
	}

}
