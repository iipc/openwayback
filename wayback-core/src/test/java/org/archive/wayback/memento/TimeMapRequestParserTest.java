package org.archive.wayback.memento;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.apache.commons.collections.map.LinkedMap;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.util.webapp.RequestMapper;
import org.archive.wayback.webapp.AccessPoint;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test for TimeMapRequestParser.
 * <p>
 * Note: this test is just accepting implemented buggy behavior. See comments
 * for more desirable behavior.
 * </p>
 */
public class TimeMapRequestParserTest extends TestCase {

	HttpServletRequest httpRequest;
	// XXX AccessPoint is necessary just because RequestMapper#getRequestContextPathQuery
	// is called through it.
	AccessPoint accessPoint;
	BaseRequestParser brp;

	protected void setUp() throws Exception {
		super.setUp();

		accessPoint = new AccessPoint();
		accessPoint.setEnableMemento(true);

		brp = new BaseRequestParser() {
			public WaybackRequest parse(HttpServletRequest httpRequest,
					AccessPoint wbContext)
					throws BadQueryException, BetterRequestException {
				return null;
			}
		};
	}

	final static String CONTEXT_PREFIX = "/web/";

	// IAnswer that takes one argument and returns value from given map.
	static class AnswerMapValue<K, V> implements IAnswer<V> {
		private final Map<K, V> map;

		public AnswerMapValue(Map<K, V> map) {
			this.map = map;
		}

		@Override
		public V answer() {
			if (map == null)
				return null;
			V value = map.get(EasyMock.getCurrentArguments()[0]);
			return value;
		}
	}

	/**
	 * set up {@code httpRequest} in order to respond to
	 * RequestMapper#getRequestContextPathQuery
	 * @param path
	 */
	protected void setupRequest(String path, Map<String, String> query) {
		httpRequest = EasyMock.createMock(HttpServletRequest.class);

		EasyMock
			.expect(
				httpRequest.getAttribute(RequestMapper.REQUEST_CONTEXT_PREFIX))
			.andStubReturn(CONTEXT_PREFIX);
		EasyMock.expect(httpRequest.getRequestURI())
			.andStubReturn(CONTEXT_PREFIX + path);
		if (query != null && !query.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> ent : query.entrySet()) {
				if (sb.length() > 0) {
					sb.append("&");
				}
				try {
					sb.append(URLEncoder.encode(ent.getKey(), "utf-8"));
					if (ent.getValue() != null) {
						sb.append("=")
							.append(URLEncoder.encode(ent.getValue(), "utf-8"));
					}
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				}
			}
			EasyMock.expect(httpRequest.getQueryString())
				.andStubReturn(sb.toString());
			EasyMock
				.expect(httpRequest.getParameter(EasyMock.<String>notNull()))
				.andAnswer(new AnswerMapValue<String, String>(query))
				.anyTimes();
		} else {
			EasyMock.expect(httpRequest.getQueryString()).andStubReturn(null);
			EasyMock
				.expect(httpRequest.getParameter(EasyMock.<String>notNull()))
				.andStubReturn(null);
		}

		EasyMock.replay(httpRequest);
	}

	protected void setupRequest(String path) {
		setupRequest(path, null);
	}

//	protected void setupRequest(String )
	public void testBaseOnly() throws Exception {
		setupRequest("timemap");

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNull(wbr);
	}

	public void testBaseTrailingSlash() throws Exception {
		setupRequest("timemap/");

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNull(wbr);
	}

	public void testBaseAndParameter() throws Exception {
		setupRequest("timemap",
			Collections.singletonMap("url", "http://example.com/"));

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		// TODO: could return default format name instead of null
		assertEquals(null, wbr.getMementoTimemapFormat());
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	// path ends with trailing slash. no format, no URL.
	// getMementoTimemapFormat() returns null, meaning "default"
	public void testBaseTrailingSlashAndParameterURL() throws Exception {
		setupRequest("timemap/",
			Collections.singletonMap("url", "http://example.com/"));

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		assertEquals(null, wbr.getMementoTimemapFormat());
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	// format in path no "output" query parameter.
	public void testPathFormatAndParameterURL() throws Exception {
		setupRequest("timemap/cdx",
			Collections.singletonMap("url", "http://example.com/"));

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		assertEquals("cdx", wbr.getMementoTimemapFormat());
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	// path ends with slash after format. target URL is taken from "url" parameter.
	public void testPathFormatTrailingSlashAndParameterURL() throws Exception {
		setupRequest("timemap/cdx/",
			Collections.singletonMap("url", "http://example.com/"));

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		assertEquals("cdx", wbr.getMementoTimemapFormat());
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	// format in path. "output" parameter is ignored.
	public void testPathFormatAndParameterURLAndOutput() throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, String> params = new LinkedMap(); // ordered map
		params.put("url", "http://example.com/");
		params.put("output", "link");
		setupRequest("timemap/cdx", params);

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		// format in path takes precedence over parameter
		// XXX BUGGY: supposed to return "cdx", but currently returns bad value
		//assertEquals("cdx", wbr.getMementoTimemapFormat());
		assertEquals("cdx", //?url=http%3A%2F%2Fexample.com%2F&output=link",
			wbr.getMementoTimemapFormat());
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	// path has format and target URL. query is part of target URL.
	public void testPathFormatAndURL() throws Exception {
		// reusing setupRequest(), but parameters are part of target URL in this case.
		@SuppressWarnings("unchecked")
		Map<String, String> params = new LinkedMap(); // ordered map
		params.put("url", "http://archive.org/");
		params.put("output", "something");
		setupRequest("timemap/link/http://example.com/post", params);

		TimeMapRequestParser parser = new TimeMapRequestParser(brp);
		WaybackRequest wbr = parser.parse(httpRequest, accessPoint);

		assertNotNull(wbr);
		assertTrue(wbr.isCaptureQueryRequest());
		assertEquals("link", wbr.getMementoTimemapFormat());
		assertEquals(
			"http://example.com/post?url=http%3A%2F%2Farchive.org%2F&output=something",
			wbr.getRequestUrl());
	}
}
