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

package org.archive.wayback.replay.html;

import junit.framework.TestCase;

import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.ReplayRewriteContext;
import org.archive.wayback.replay.ReplayURLTransformer;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Tests for {@link ReplayParseContext}.
 */
public class ReplayParseContextTest extends TestCase {

	ReplayParseContext cut;

	WaybackRequest wbRequest;
	CaptureSearchResult result;
	ReplayURIConverter uriConverter;
	ReplayURLTransformer urlTransformer;

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		wbRequest = new WaybackRequest();

		result = new CaptureSearchResult();
		result.setOriginalUrl("http://www.example.com/top/index.html");
		result.setCaptureTimestamp("20100203112233");

		// set up a stub mock whose getURLTransformer() returns
		// an object set to urlTransformer.
		uriConverter = EasyMock.createMock(ReplayURIConverter.class);
		EasyMock.expect(uriConverter.getURLTransformer()).andStubAnswer(
			new IAnswer<ReplayURLTransformer>() {
				@Override
				public ReplayURLTransformer answer() throws Throwable {
					return urlTransformer;
				}
			});
	}

	public void testResolve() throws Exception {
		cut = new ReplayParseContext(uriConverter, result, wbRequest);
		// uriConverter is not involved.
		{
			String resolved = cut.resolve("http://archive.org/help.html");
			assertEquals("http://archive.org/help.html", resolved);
		}
		{
			String resolved = cut.resolve("//archive.org/help.html");
			assertEquals("http://archive.org/help.html", resolved);
		}
		{
			String resolved = cut.resolve("/help.html");
			assertEquals("http://www.example.com/help.html", resolved);
		}
		{
			String resolved = cut.resolve("help.html");
			assertEquals("http://www.example.com/top/help.html", resolved);
		}
		// special cases
		{
			String resolved = cut.resolve("javascript:alert()");
			assertEquals("javascript:alert()", resolved);
		}
		{
			String resolved = cut.resolve("/help.html#section1");
			assertEquals("http://www.example.com/help.html#section1", resolved);
		}
	}

	public void testMakeReplayURI() throws Exception {
		final String DATESPEC = result.getCaptureTimestamp();
		final String URL = "http://archive.org/help/contents.html";
		final String FLAGS = "fw_";
		final URLStyle URLSTYLE = URLStyle.SERVER_RELATIVE;

		EasyMock.expect(
			uriConverter.makeReplayURI(DATESPEC, URL, FLAGS, URLSTYLE))
			.andReturn("/web/" + DATESPEC + FLAGS + "/" + URL);

		EasyMock.replay(uriConverter);

		cut = new ReplayParseContext(uriConverter, result, wbRequest);

		cut.makeReplayURI(URL, FLAGS, URLSTYLE);
	}

	public void testGetContextFlags() throws Exception {
		wbRequest.setFrameWrapperContext(true);

		EasyMock.replay(uriConverter);

		cut = new ReplayParseContext(uriConverter, result, wbRequest);

		String flags = cut.getContextFlags();

		assertEquals("fw_", flags);
	}

	public void testContextualizeUrl() throws Exception {
		cut = ReplayParseContext.create(uriConverter, wbRequest, null, result, false);
	}

	// tests for compatibility with previous versions

	/**
	 * If WaybackRequest is not given ({@code null}),
	 * {@link ReplayParseContext#getContextFlags()} simply returns {@code null}.
	 * @throws Exception
	 */
	public void testGetContextFlags_compat() throws Exception {
		EasyMock.replay(uriConverter);

		cut = new ReplayParseContext(uriConverter, result, null);

		String flags = cut.getContextFlags();

		assertNull(flags);
	}

	/**
	 * Initialized with non-ReplayURIConverter, and {@code null} for {@code converterFactory},
	 * ReplayParseContext uses old {@link ResultURIConverter#makeReplayURI(String, String)}
	 * through IdentityResultURIConverterFactory, which simply ignores context flags.
	 * @throws Exception
	 */
	public void testContextualizeUrl_compat() throws Exception {
		final String OURL = "/help/content.html";
		final String URL = "http://www.example.com" + OURL;
		final String AURL = "http://web.archive.org/web/" + result.getCaptureTimestamp() + "/" + URL;
		// hiding member field
		ResultURIConverter uriConverter = EasyMock.createMock(ResultURIConverter.class);
		EasyMock.expect(
			uriConverter.makeReplayURI(result.getCaptureTimestamp(), URL))
			.andReturn(AURL);

		EasyMock.replay(uriConverter);

		cut = ReplayParseContext.create(uriConverter, wbRequest, null, result, false);

		// HEADER_CONTEXT is canceled
		String aurl = cut.contextualizeUrl(OURL, ReplayRewriteContext.HEADER_CONTEXT);

		assertEquals(AURL, aurl);

		EasyMock.verify(uriConverter);
	}

	/**
	 * Old method of passing context flags to URL rewrite.
	 * <p>
	 * Now PreservingHttpHeaderProcessor passes special {@code HEADER_CONTEXT}
	 * to {@link ReplayRewriteContext#contextualizeUrl(String, String)} when
	 * rewriting {@code Location} header. This value must be replaced with
	 * context flags in the request before calling
	 * {@link ResultURIConverter#makeReplayURI(String, String)}.
	 * </p>
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public void testContextualizeUrl_factory() throws Exception {
		ArchivalUrlResultURIConverter uriConverter = new ArchivalUrlResultURIConverter();
		uriConverter.setReplayURIPrefix("http://web.archive.org/web/");

		cut = ReplayParseContext.create(uriConverter, wbRequest, null, result, false);

		String aurl = cut.contextualizeUrl("/help/content.html", "fw_");

		assertEquals("http://web.archive.org/web/" +
				result.getCaptureTimestamp() + "fw_/" +
				"http://www.example.com/help/content.html", aurl);

		// HEADER_CONTEXT is replaced with context flags in the request
		wbRequest.setIFrameWrapperContext(true);
		aurl = cut.contextualizeUrl("/help/content.html", ReplayRewriteContext.HEADER_CONTEXT);

		assertEquals("http://web.archive.org/web/" +
				result.getCaptureTimestamp() + "if_/" +
				"http://www.example.com/help/content.html", aurl);
	}
}
