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
import java.net.URL;
import java.util.ArrayList;

import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class JSStringTransformerTest extends TestCase {

	URL baseURL;
	// TODO: extract interface from ReplayParseContext and
	// use EasyMock instead of hand-writing mock object.
	RecordingReplayParseContext rc;
	JSStringTransformer jst;

	@Override
	protected void setUp() throws Exception {
		baseURL = new URL("http://foo.com");
		rc = new RecordingReplayParseContext(null, baseURL, null);
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
	 * {@code rewriteHttpsOnly} property is used to limit URL rewrite
	 * to HTTPS ones (intended for proxy mode). That should affect how
	 * StringTransformer picks up URLs in text for translation.
	 * @throws Exception
	 */
	public void testRewriteHttpsOnly() throws Exception {
		rc.setRewriteHttpsOnly(true);
		
		final String input = "var img1 = 'http://example.com/img/1.jpeg';\n" +
				"var img2 = 'https://secure1.example.com/img/2.jpeg';\n" +
				"var img3 = '/img/3.jpeg';\n" +
				"var host1 = 'http://example.com';\n" +
				"var host2 = 'https://secure2.example.com';\n";

		jst.transform(rc, input);

		assertEquals(2, rc.got.size());
		// with default regex, JSStringTransformer captures
		// scheme and netloc only (no path).
		assertTrue(rc.got.contains("https://secure1.example.com"));
		assertTrue(rc.got.contains("https://secure2.example.com"));
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
	 * ReplayParseContext mock
	 * TODO: move to package-level as this is useful for testing other
	 * {@code StringTransformer}s.
	 */
	public static class RecordingReplayParseContext extends ReplayParseContext {
		ArrayList<String> got = null;
		/**
		 * @param uriConverterFactory
		 * @param baseUrl
		 * @param datespec
		 */
		public RecordingReplayParseContext(
				ContextResultURIConverterFactory uriConverterFactory,
				URL baseUrl, String datespec) {
			super(uriConverterFactory, baseUrl, datespec);
			got = new ArrayList<String>();
			// TODO Auto-generated constructor stub
		}
		public String contextualizeUrl(String url) {
			got.add(url);
			return "###" + url;
		}
		@Override
		public String contextualizeUrl(String url, String flags) {
			// TODO record flags, too
			got.add(url);
			return "###" + url;
		}
	}
}
