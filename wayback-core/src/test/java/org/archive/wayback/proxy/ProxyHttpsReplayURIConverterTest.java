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
		assertEquals(input,
			cut.makeReplayURI("20140404102345", input, null, URLStyle.ABSOLUTE));
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

	public void testTransform_escapedHttps() {
		// ReplayURLTransformer shall remove backslash escapes before
		// passing it to makeReplayURI().
		final String input = "https:\\/\\/home.example.org/index.html";
		replayContext = EasyMock.createMock(ReplayContext.class);
		EasyMock.expect(replayContext.makeReplayURI(
			EasyMock.eq("https://home.example.org/index.html"),
			EasyMock.or(EasyMock.<String>isNull(), EasyMock.eq("")),
			EasyMock.<URLStyle>anyObject())).andReturn(input);
		EasyMock.replay(replayContext);
		cut.transform(replayContext, input, null);
		EasyMock.verify(replayContext);
	}

	/**
	 * {@code transform} method must call {@link ReplayContext#makeReplayURI(String, String, URLStyle)}
	 * for replay URL construction, must not call {@code makeReplayURI} in the same class directly.
	 * @throws Exception
	 */
	public void testTransformBuildsURLThroughReplayContext() throws Exception {
		final ReplayContext replayContext = EasyMock.createMock(ReplayContext.class);

		final String url = "http://example.com/";
		EasyMock.expect(replayContext.makeReplayURI(url, null, URLStyle.ABSOLUTE))
			.andReturn(url);

		EasyMock.replay(replayContext);

		cut.transform(replayContext, url, null);

		EasyMock.verify(replayContext);
	}
}
