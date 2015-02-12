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

package org.archive.wayback.archivalurl;

import java.net.URISyntaxException;
import java.util.Properties;

import junit.framework.TestCase;

import org.archive.url.HandyURL;
import org.archive.url.URLParser;
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Test for {@link ArchivalUrlReplayURIConverter}
 */
public class ArchivalUrlReplayURIConverterTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static final String TIMESTAMP = "20140102030405";

	public void testMakeReplayURI() throws Exception {
		ArchivalUrlReplayURIConverter cut = new ArchivalUrlReplayURIConverter();
		cut.setReplayURIPrefix("http://web.archive.org/web/");

		final String URL = "http://example.com/";

		{
			String url = cut.makeReplayURI(TIMESTAMP, URL, null, URLStyle.ABSOLUTE);
			assertEquals("http://web.archive.org/web/" + TIMESTAMP + "/" + URL, url);
		}
		{
			String url = cut.makeReplayURI(TIMESTAMP, URL, null, URLStyle.PROTOCOL_RELATIVE);
			assertEquals("//web.archive.org/web/" + TIMESTAMP + "/" + URL, url);
		}
		{
			String url = cut.makeReplayURI(TIMESTAMP, URL, null, URLStyle.SERVER_RELATIVE);
			assertEquals("/web/" + TIMESTAMP + "/" + URL, url);
		}
		{
			String url = cut.makeReplayURI(TIMESTAMP, URL, "js_", URLStyle.ABSOLUTE);
			assertEquals("http://web.archive.org/web/" + TIMESTAMP + "js_/" + URL, url);
		}
	}

	public void testMakeReplayURI_protocolRelativePrefix() throws Exception {
		ArchivalUrlReplayURIConverter cut = new ArchivalUrlReplayURIConverter();
		cut.setReplayURIPrefix("//web.archive.org/web/");

		final String URL = "http://example.com/";
		{
			// It returns protocol-relative despite URLStyle.FULL, because
			// replayURIPrefix is protocol-relative.
			String url = cut.makeReplayURI(TIMESTAMP, URL, null, URLStyle.ABSOLUTE);
			assertEquals("//web.archive.org/web/" + TIMESTAMP + "/" + URL, url);
		}
		{
			String url = cut.makeReplayURI(TIMESTAMP, URL, null, URLStyle.PROTOCOL_RELATIVE);
			assertEquals("//web.archive.org/web/" + TIMESTAMP + "/" + URL, url);
		}
	}
	
	/**
	 * For backward compatibility, ArchivalUrlReplayURIConverter complements
	 * relative {@code replayPrefix} with {@code aggregationPrefix} config parameter.
	 * @throws Exception
	 */
	public void testBackwardCompatibibility() throws Exception {
		final String AGGREGATION_PREFIX = "http://web.archive.org";

		ArchivalUrlReplayURIConverter cut = new ArchivalUrlReplayURIConverter();
		AccessPoint ap = new AccessPoint();
		ap.setReplayPrefix("/web/");
		ap.setConfigs(new Properties());
		ap.getConfigs().setProperty(MementoUtils.AGGREGATION_PREFIX_CONFIG, AGGREGATION_PREFIX);
		
		cut.setAccessPoint(ap);
		
		assertEquals(AGGREGATION_PREFIX + "/web/", cut.getReplayURIPrefix());
	}

	/**
	 * Confirm expected behavior of {@link HandyURL}.
	 * @throws Exception
	 */
	public void testHandyURL() throws Exception {
		HandyURL h;
		h = URLParser.parse("http://web.archive.org/web/");
		assertEquals("http", h.getScheme());
		assertEquals("web.archive.org", h.getHost());
		assertEquals(-1, h.getPort());
		assertEquals("/web/", h.getPath());
		assertEquals(null, h.getQuery());
		assertEquals(null, h.getHash());

		try {
			h = URLParser.parse("http:/web.archive.org/web/");
			fail();
//			assertEquals("http", h.getScheme());
//			assertEquals("web.archive.org", h.getHost());
//			assertEquals(-1, h.getPort());
//			assertEquals("/web/", h.getPath());
		} catch (URISyntaxException ex) {
			// expected
		}

		h = URLParser.parse("http://web.archive.org:8080/web/");
		assertEquals("http", h.getScheme());
		assertEquals("web.archive.org", h.getHost());
		assertEquals(8080, h.getPort());
		assertEquals("/web/", h.getPath());

		h = URLParser.parse("//web.archive.org/web/");
		assertEquals("http", h.getScheme());
		assertEquals("web.archive.org", h.getHost());
		assertEquals("/web/", h.getPath());

		h = URLParser.parse("/web/");
		assertEquals("http", h.getScheme());
		assertEquals("web", h.getHost());
		assertEquals("/", h.getPath());

		h = URLParser.parse("web/");
		assertEquals("http", h.getScheme());
		assertEquals("web", h.getHost());
		assertEquals("/", h.getPath());

		h = URLParser.parse("///web/");
		assertEquals("http", h.getScheme());
		assertEquals("web", h.getHost());
		assertEquals("/", h.getPath());
	}
}
