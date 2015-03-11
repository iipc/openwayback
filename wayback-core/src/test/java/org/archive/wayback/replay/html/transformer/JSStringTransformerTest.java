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
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.proxy.ProxyHttpsReplayURIConverter;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayURLTransformer;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.IdentityResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;

/**
 * @author brad
 *
 */
public class JSStringTransformerTest extends TestCase {

	String baseURL;
	// TODO: extract interface from ReplayParseContext and
	// use EasyMock instead of hand-writing mock object.
	RecordingReplayParseContext rc;
	JSStringTransformer jst;

	@Override
	protected void setUp() throws Exception {
		baseURL = "http://foo.com";
		rc = new RecordingReplayParseContext(baseURL, null);
		jst = new JSStringTransformer();
	}

	/**
	 * Test method for {@link org.archive.wayback.replay.html.transformer.JSStringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)}.
	 * @throws MalformedURLException
	 */
	public void testTransform_HostOnly() throws MalformedURLException {
		String input = "'<a href=\'http://www.gavelgrab.org\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		JSStringTransformer jst = new JSStringTransformer();
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http://www.gavelgrab.org",rc.got.get(0));
	}

	public void testTransform_WithPath() {
		final String input = "'<a href=\'http://www.gavelgrab.org/foobla/blah\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http://www.gavelgrab.org",rc.got.get(0));
	}

	/**
	 * slash is often escaped with backslash in JavaScript (esp. JSON).
	 */
	public void testTransform_EscapedSlashes() {

		final String input = "onloadRegister(function (){window.location.href=\"http:\\/\\/www.facebook.com\\/barrettforwisconsin?v=info\";});";
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http:\\/\\/www.facebook.com",rc.got.get(0));
	}

	/**
	 * same as above, slashes are backslash-escaped.
	 * @throws Exception
	 * (this is a test for old URL rewrite framework).
	 */
	public void testRewriteHttpsOnlyEscapedSlashes() throws Exception {
		// using custom RecordingReplayParseContext for testing actual rewrite. This is more than
		// a unit test of JSStringTransformer, but it is useful to capture bugs caused by inconsistency
		// among JSStringTransformer, ReplayParseContext and ResultURIConverterFactory (hopefully
		// they should be refactored into coherent, easier-to-test components.) this is a common
		// setup for proxy-mode (IdentityResultURIConverterFactory returns ProxyHttpsResultURIConverter.)
		ProxyHttpsReplayURIConverter urlTransformer = new ProxyHttpsReplayURIConverter();
		urlTransformer.setRewriteHttps(true);
		rc = new RecordingReplayParseContext(urlTransformer, baseURL, null);

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
	 * same as above, slashes are backslash-escaped.
	 * @throws Exception
	 * (this is a test for old URL rewrite framework).
	 */
	public void testOldRewriteHttpsOnlyEscapedSlashes() throws Exception {
		// using custom RecordingReplayParseContext for testing actual rewrite. This is more than
		// a unit test of JSStringTransformer, but it is useful to capture bugs caused by inconsistency
		// among JSStringTransformer, ReplayParseContext and ResultURIConverterFactory (hopefully
		// they should be refactored into coherent, easier-to-test components.) this is a common
		// setup for proxy-mode (IdentityResultURIConverterFactory returns ProxyHttpsResultURIConverter.)
		IdentityResultURIConverterFactory uriConverterFactory = new IdentityResultURIConverterFactory();
		rc = new RecordingReplayParseContext(uriConverterFactory, baseURL, null);
		rc.setRewriteHttpsOnly(true);

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

		String output = jst.transform(rc, input);

		assertEquals(2, rc.got.size());
		assertTrue(rc.got.contains("//platform.twitter.com"));
		assertTrue(rc.got.contains("https://platform2.twitter.com"));

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

		// throws an exception if replacement text is not properly escaped.
		String output = jst.transform(rc, input);

		assertEquals(1, rc.got.size());
		assertEquals("http://www.example.com\\", rc.got.get(0));

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
	 * ReplayParseContext mock
	 * TODO: move to package-level as this is useful for testing other
	 * {@code StringTransformer}s.
	 */
	public static class RecordingReplayParseContext extends ReplayParseContext {
		ArrayList<String> got = null;
		private static CaptureSearchResult capture(String baseUrl, String datespec) {
			CaptureSearchResult r = new CaptureSearchResult();
			r.setCaptureTimestamp(datespec);
			r.setOriginalUrl(baseUrl);
			return r;
		}

		public RecordingReplayParseContext(String baseUrl, String datespec) {
			this(new StubReplayURIConverter(), baseUrl, datespec);
		}

		/**
		 * @param uriConverter
		 * @param baseUrl
		 * @param datespec
		 */
		public RecordingReplayParseContext(ReplayURIConverter uriConverter, String baseUrl, String datespec) {
			super(uriConverter, capture(baseUrl, datespec));
			got = new ArrayList<String>();
		}

		/**
		 * Compatibility mode constructor
		 * @param uriConverterFactory must not be {@code null}
		 * @param baseUrl
		 * @param datespec
		 */
		public RecordingReplayParseContext(
				ContextResultURIConverterFactory uriConverterFactory,
				String baseUrl, String datespec) {
			super(uriConverterFactory, capture(baseUrl, datespec));
			got = new ArrayList<String>();
		}
		@Override
		public String contextualizeUrl(String url, String flags) {
			// TODO record flags, too
			got.add(url);
			return super.contextualizeUrl(url, flags);
		}
	}
}
