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
package org.archive.wayback.replay.html.transformer;

import java.net.MalformedURLException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.archivalurl.ArchivalUrlReplayURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.proxy.ProxyHttpsReplayURIConverter;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayURLTransformer;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.IdentityResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * @author brad
 *
 */
public class JSStringTransformerTest extends TestCase {

	String baseURL;
	// TODO: extract interface from ReplayParseContext and
	// use EasyMock instead of hand-writing mock object.
	//RecordingReplayParseContext rc;
	JSStringTransformer jst;

	ReplayParseContextMock rpc;
	CaptureSearchResult result;

	IAnswer<String> contextualizeUrlAnswer;


	@Override
	protected void setUp() throws Exception {
		baseURL = "http://foo.com";
		jst = new JSStringTransformer();

		result = new CaptureSearchResult();
		result.setOriginalUrl("http://foo.com/");
		result.setCaptureTimestamp("20100101020304");

		rpc = new ReplayParseContextMock();

		contextualizeUrlAnswer = new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				return "###" + (String)EasyMock.getCurrentArguments()[0];
			}
		};
	}

	/**
	 * Test method for {@link org.archive.wayback.replay.html.transformer.JSStringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)}.
	 * @throws MalformedURLException
	 */
	public void testTransform_HostOnly() throws MalformedURLException {
		String input = "'<a href=\'http://www.gavelgrab.org\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		EasyMock.expect(
			rpc.mock.contextualizeUrl("http://www.gavelgrab.org", ""))
			.andAnswer(contextualizeUrlAnswer);
		EasyMock.replay(rpc.mock);

		jst.transform(rpc, input);
	}

	public void testTransform_WithPath() {
		final String input = "'<a href=\'http://www.gavelgrab.org/foobla/blah\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		EasyMock.expect(
			rpc.mock.contextualizeUrl("http://www.gavelgrab.org", ""))
			.andAnswer(contextualizeUrlAnswer);
		EasyMock.replay(rpc.mock);

		jst.transform(rpc, input);
	}

	/**
	 * slash is often escaped with backslash in JavaScript (esp. JSON).
	 */
	public void testTransform_EscapedSlashes() {
		final String input = "onloadRegister(function (){" +
				"window.location.href=\"" +
				"http:\\/\\/www.facebook.com\\/barrettforwisconsin?v=info\";" +
				"});";
		// replay prefix is not escaped, and original URL is unescaped by
		// HandyURL. rewritten URL is inserted without escaping.
		final String expected = "onloadRegister(function (){" +
				"window.location.href=\"" +
				"http://web.archive.org/web/20100101020304/" +
				"http://www.facebook.com\\/barrettforwisconsin?v=info\";" +
				"});";
		// contextualizeUrl will receive URl as it appears in the resource
		EasyMock
			.expect(rpc.mock.contextualizeUrl("http:\\/\\/www.facebook.com", ""))
			.andReturn(
				"http://web.archive.org/web/20100101020304/http://www.facebook.com");

		EasyMock.replay(rpc.mock);

		String output = jst.transform(rpc, input);

		assertEquals(expected, output);
	}

	/**
	 * test of {@link JSStringTransformer#setEscaping(String)}
	 */
	public void testTransform_Escaping() {
		final String input = "onloadRegister(function (){" +
				"window.location.href=\"" +
				"http:\\/\\/www.facebook.com\\/barrettforwisconsin?v=info\";" +
				"});";
		// rewritten URL is escaped back
		final String expected = "onloadRegister(function (){" +
				"window.location.href=\"" +
				"http:\\/\\/web.archive.org\\/web\\/20100101020304\\/" +
				"http:\\/\\/www.facebook.com\\/barrettforwisconsin?v=info\";" +
				"});";
		// contextualizeUrl will receive unescaped URL
		EasyMock
			.expect(rpc.mock.contextualizeUrl("http://www.facebook.com", ""))
			.andReturn(
				"http://web.archive.org/web/20100101020304/http://www.facebook.com");

		EasyMock.replay(rpc.mock);

		jst.setEscaping("JavaScript");
		String output = jst.transform(rpc, input);

		assertEquals(expected, output);
	}

	/**
	 * {@link JSStringTransformer#setEscaping(String)} shall throw
	 * IllegalArgumentException for undefined escaping scheme name.
	 */
	public void testTransform_EscapingUndefinedName() {
		try {
			jst.setEscaping("Bogus");
			fail("setEscaping did not throw an exception");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

	/**
	 * same as above, slashes are backslash-escaped.
	 * @throws Exception
	 */
	public void testRewriteHttpsOnlyEscapedSlashes() throws Exception {
		// using custom RecordingReplayParseContext for testing actual rewrite. This is more than
		// a unit test of JSStringTransformer, but it is useful to capture bugs caused by inconsistency
		// among JSStringTransformer, ReplayParseContext and ResultURIConverterFactory (hopefully
		// they should be refactored into coherent, easier-to-test components.) this is a common
		// setup for proxy-mode (IdentityResultURIConverterFactory returns ProxyHttpsResultURIConverter.)
		ReplayURIConverter uriConverter = new ProxyHttpsReplayURIConverter();
		ReplayParseContext rc = new ReplayParseContext(uriConverter, result);
		// Note: ParseContext.resolve(String) uses UsableURIFactory.getInstance() for
		// making URL absolute. It not only prepends baseURL but also removes escaping
		// like "\/", "%3A". So, depending on the URL pattern, more "\/" may be replaced
		// by "/" (as the default pattern matches scheme and netloc only, "\/" in
		// path part is retained here). ResultURIConverter
		final String input = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'https:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var host1 = 'http:\\/\\/example.com';\n" +
				"var host2 = 'https:\\/\\/secure2.example.com';\n";
		final String expected = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'http://secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var host1 = 'http:\\/\\/example.com';\n" +
				"var host2 = 'http://secure2.example.com';\n";

		String out = jst.transform(rc, input);

		assertEquals(expected, out);
	}

	/**
	 * same as above, slashes are backslash-escaped. This version uses new unescape-escape
	 * method. In this case, JSStringTransformer escapes slashes not escaped in the input.
	 * Test also include alternative escaping form {@code \u002F}.
	 * @throws Exception
	 */
	public void testRewriteHttpsOnlyEscapedSlashesUnescaping() throws Exception {
		ReplayURIConverter uriConverter = new ProxyHttpsReplayURIConverter();
		ReplayParseContext rc = new ReplayParseContext(uriConverter, result);

		final String input = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'https:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var host1 = 'http:\\u002F\\u002Fexample.com';\n" +
				"var host2 = 'https:\\u002F\\u002Fsecure2.example.com';\n";
		final String expected = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'http:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				// original escaping is retained, because URL is not rewritten (http)
				"var host1 = 'http:\\u002F\\u002Fexample.com';\n" +
				// \u002F gets repalced with \/ because URL is rewritten (https)
				"var host2 = 'http:\\/\\/secure2.example.com';\n";

		// non-default regexp to match URLs with escaped slashes.
		jst.setRegex("(https?(?::|%3A)(/|\\\\/|%2F|\\\\u002F|\\\\u00252F)\\2[-A-Za-z0-9:_@.]+)");
		jst.setEscaping("JavaScript");
		String out = jst.transform(rc, input);

		assertEquals(expected, out);
	}

	/**
	 * Similarly, tests actual rewrite with ArchivalUrlReplayURIConverter, using
	 * unescape-escape method.
	 * @throws Exception
	 */
	public void testRewriteArchivalUrl() throws Exception {
		ArchivalUrlReplayURIConverter uriConverter = new ArchivalUrlReplayURIConverter();
		uriConverter.setReplayURIPrefix("http://web.archive.org/web/");

		ReplayParseContext rc = new ReplayParseContext(uriConverter, result);

		final String input = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'https:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var img4 = '\\u002F\\u002Fexample.com\\u002Fimg\\u002F4.png;\n" +
				"var host1 = 'http:\\u002F\\u002Fexample.com';\n" +
				"var host2 = 'https:\\u002F\\u002Fsecure2.example.com';\n";
		final String expected = "var img1 = 'http:\\/\\/web.archive.org\\/web\\/20100101020304\\/http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'http:\\/\\/web.archive.org\\/web\\/20100101020304\\/https:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				// \u002F in path part remains unchanged
				"var img4 = '\\/\\/web.archive.org\\/web\\/20100101020304\\/http:\\/\\/example.com\\u002Fimg\\u002F4.png;\n" +
				// \u002F gets replaced with \/ because URL is rewritten
				"var host1 = 'http:\\/\\/web.archive.org\\/web\\/20100101020304\\/http:\\/\\/example.com';\n" +
				"var host2 = 'http:\\/\\/web.archive.org\\/web\\/20100101020304\\/https:\\/\\/secure2.example.com';\n";

		// non-default regexp to match URLs with escaped slashes (also matches protocol-relative)
		jst.setRegex("((?:https?(?::|%3A))?(/|\\\\/|%2F|\\\\u002F|\\\\u00252F)\\2[-A-Za-z0-9:_@.]+)");
		jst.setEscaping("JavaScript");
		String out = jst.transform(rc, input);

		assertEquals(expected, out);
	}

	/**
	 * yet another backslash-escaped case. testing old URL rewrite framework
	 * for backward compatibility purpose.
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void testOldRewriteHttpsOnlyEscapedSlashes() throws Exception {
		IdentityResultURIConverterFactory uriConverterFactory = new IdentityResultURIConverterFactory();
		ReplayParseContext rc = new ReplayParseContext(uriConverterFactory, result);
		rc.setRewriteHttpsOnly(true);

		final String input = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'https:\\/\\/secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var host1 = 'http:\\/\\/example.com';\n" +
				"var host2 = 'https:\\/\\/secure2.example.com';\n";
		final String expected = "var img1 = 'http:\\/\\/example.com\\/img\\/1.jpeg';\n" +
				"var img2 = 'http://secure1.example.com\\/img\\/2.jpeg';\n" +
				"var img3 = '\\/img\\/3.jpeg';\n" +
				"var host1 = 'http:\\/\\/example.com';\n" +
				"var host2 = 'http://secure2.example.com';\n";

		String out = jst.transform(rc, input);

		assertEquals(expected, out);
	}


	/**
	 * test of rewriting protocol relative URLs ({@code "//www.example.com/..."})
	 * with non-default regex.
	 * <p>check if text preceding the first group is preserved in the result.
	 * also make sure it works with URLs with protocol.</p>
	 * @throws Exception
	 */
	public void testRewriteProtocolRelativeWithCustomRegex() throws Exception {
		jst.setRegex("[\"']((?:https?:)?//(?:[^/]+@)?[^@:/]+(?:\\.[^@:/]+)+(?:[0-9]+)?)");

		final String input = "js=d.createElement(s);js.id=id;js.src=\"//platform.twitter.com/widgets.js\";" +
				"js.src2=\"https://platform2.twitter.com/widgets2.js\";" +
				"fjs.parentNode.insertBefore(js,fjs);";
		final String expected = "js=d.createElement(s);js.id=id;js.src=\"###//platform.twitter.com/widgets.js\";" +
				"js.src2=\"###https://platform2.twitter.com/widgets2.js\";" +
				"fjs.parentNode.insertBefore(js,fjs);";

		EasyMock.expect(
			rpc.mock.contextualizeUrl("//platform.twitter.com", ""))
			.andAnswer(contextualizeUrlAnswer);
		EasyMock.expect(
			rpc.mock.contextualizeUrl(
				"https://platform2.twitter.com", "")).andAnswer(
			contextualizeUrlAnswer);
		EasyMock.replay(rpc.mock);

		String output = jst.transform(rpc, input);

		assertEquals(expected, output);
	}

	/**
	 * test of rewriting corner case where URL contains special chars for
	 * {@code Matcher#appendReplacement}.
	 *
	 * @throws Exception
	 */
	public void testRewriteSpecialCharURL() throws Exception {
		// using custom regex, as default pattern does not allow backslash in URL.
		// this regex also deliberately excludes single quote so that replacement
		// text ends with backslash.
		jst.setRegex("[\"']((?:https?:)?//(?:[^/]+@)?[^@:/']+(?:\\.[^@:/']+)+(?:[0-9]+)?)");
		final String input = "var b='http://www.example.com\\'";
		final String expected = "var b='###http://www.example.com\\'";

		EasyMock.expect(
			rpc.mock.contextualizeUrl("http://www.example.com\\", ""))
			.andAnswer(contextualizeUrlAnswer);
		EasyMock.replay(rpc.mock);

		// throws an exception if replacement text is not properly escaped.
		String output = jst.transform(rpc, input);

		assertEquals(expected, output);
	}

	public static class StubReplayURIConverter implements ReplayURIConverter,
			ReplayURLTransformer {

		@Override
		public String makeReplayURI(String datespec, String url) {
			return url;
		}

		@Override
		public String makeReplayURI(String datespec, String url, String flags,
				URLStyle urlStyle) {
			return url;
		}

		@Override
		public ReplayURLTransformer getURLTransformer() {
			return this;
		}

		@Override
		public String transform(ReplayContext replayContext, String url,
				String contextFlags) {
			// prepend chars to signify it's rewritten.
			return "###" + url;
		}

	}

	/**
	 * An interface for mocking {@link ReplayParseContext}.
	 * Create a mock of this interface, and set
	 */
	public interface IReplayParseContext extends ReplayContext {
		public String contextualizeUrl(String url, String flags);
	}

	public static class ReplayParseContextMock extends ReplayParseContext {
		public final IReplayParseContext mock;

		private static CaptureSearchResult dummyCapture() {
			CaptureSearchResult result = new CaptureSearchResult();
			// at least originalUrl must be a valid URL, or ReplayParseContext
			// constructor will fail.
			result.setCaptureTimestamp("20100101020304");
			result.setOriginalUrl("http://foo.com/");
			return result;
		}

		public ReplayParseContextMock() {
			this(EasyMock.createMock(IReplayParseContext.class));
		}

		public ReplayParseContextMock(IReplayParseContext mock) {
			this(mock, new ProxyHttpsReplayURIConverter(), dummyCapture());
		}

		public ReplayParseContextMock(IReplayParseContext mock,
				ReplayURIConverter uriConverter, CaptureSearchResult result) {
			// these values must be non-null, but unused in tests.
			// passed just to keep ReplayParseContext constructor happy.
			super(uriConverter, result);
			this.mock = mock;
		}

		@Override
		public String contextualizeUrl(String url, String flags) {
			return mock.contextualizeUrl(url, flags);
		}
	}
}
