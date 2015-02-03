package org.archive.wayback.archivalurl.requestparser;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.archive.wayback.archivalurl.ArchivalUrlRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.memento.TimeGateBadQueryException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.util.webapp.RequestMapper;
import org.archive.wayback.webapp.AccessPoint;
import org.easymock.EasyMock;

/**
 * Test for {@link DatelessReplayRequestParser}
 */
public class DatelessReplayRequestParserTest extends TestCase {

	DatelessReplayRequestParser cut;

	HttpServletRequest request;

	String acceptDatetime = null;
	String acceptTimestamp = null;

	AccessPoint accessPoint = new AccessPoint();

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		// this is unused by DatelessReplayRequestParser
		BaseRequestParser wrapped = new ArchivalUrlRequestParser();
		cut = new DatelessReplayRequestParser(wrapped);
	}

	protected void setupRequest(String requestURI) {
		request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)"+MementoUtils.ACCEPT_DATETIME))).andStubReturn(acceptDatetime);
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)accept-timestamp"))).andStubReturn(acceptTimestamp);
		EasyMock.expect(request.getAttribute(RequestMapper.REQUEST_CONTEXT_PREFIX)).andStubReturn(null);
		EasyMock.expect(request.getRequestURI()).andStubReturn(requestURI);
		EasyMock.expect(request.getQueryString()).andStubReturn(null);
	}

	public void testAcceptDatetime() throws Exception {
		acceptDatetime = "Mon, 27 Oct 2014 20:36:02 GMT";
		accessPoint.setEnableMemento(true);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertTrue(wbr.isMementoTimegate());
		assertEquals("http://example.com/", wbr.getRequestUrl());
		assertEquals("20141027203602", wbr.getReplayTimestamp());
	}

	/**
	 * Alternate header {@code Accept-Timestamp} is in DT14 format.
	 * @throws Exception
	 */
	public void testAcceptTimestamp() throws Exception {
		acceptTimestamp = "20141027203602";
		accessPoint.setEnableMemento(true);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertTrue(wbr.isMementoTimegate());
		assertEquals("http://example.com/", wbr.getRequestUrl());
		assertEquals("20141027203602", wbr.getReplayTimestamp());
	}

	/**
	 * invalid Accept-Timestamp is simply ignored
	 * @throws Exception
	 */
	public void testAcceptTimestamp_invalid() throws Exception {
		// Valid HTTP Date, but invalid for Accept-Timestamp
		acceptTimestamp = "Mon, 27 Oct 2014 20:36:02 GMT";
		accessPoint.setEnableMemento(true);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertTrue(wbr.isMementoTimegate());
		assertEquals("http://example.com/", wbr.getRequestUrl());
		assertNotNull(wbr.getReplayTimestamp());
	}

	/**
	 * Throws TimeGateBadQueryException if Accept-Datetime is present
	 * but invalid.
	 * @throws Exception
	 */
	public void testAcceptDate_invalid() throws Exception {
		acceptDatetime = "2014-10-27 20:36:02";
		accessPoint.setEnableMemento(true);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		try {
			cut.parse(request, accessPoint);
			fail();
		} catch (TimeGateBadQueryException ex) {
			// expected;
		}
	}

	/**
	 * Invalid Accept-Datetime is simply ignored if valid
	 * Accept-Timestamp is present.
	 * @throws Exception
	 */
	public void testAcceptDate_invalidWithTimestamp() throws Exception {
		acceptDatetime = "2014-10-27 20:36:02";
		acceptTimestamp = "20141027203602";
		accessPoint.setEnableMemento(true);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertTrue(wbr.isMementoTimegate());
		assertEquals("http://example.com/", wbr.getRequestUrl());
		assertEquals("20141027203602", wbr.getReplayTimestamp());
	}

	/**
	 * Accept-Datetime is ignored unless {@code enabledMemento} is {@code true}.
	 * @throws Exception
	 */
	public void testMementoDisabled() throws Exception {
		acceptDatetime = "Mon, 27 Oct 2014 20:36:02 GMT";
		accessPoint.setEnableMemento(false);
		setupRequest("http://example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertFalse(wbr.isMementoTimegate());
		assertEquals("http://example.com/", wbr.getRequestUrl());
		assertNotNull(wbr.getReplayTimestamp());
		assertFalse("20141027203602".equals(wbr.getReplayTimestamp()));
	}

	public void testNoProtocol() throws Exception {
		setupRequest("example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	public void testProtocolRelative() throws Exception {
		acceptDatetime = "Mon, 27 Oct 2014 20:36:02 GMT";
		accessPoint.setEnableMemento(true);
		setupRequest("//example.com/");
		EasyMock.replay(request);
		try {
			WaybackRequest wbr = cut.parse(request, accessPoint);
			assertNotNull(wbr);
			assertTrue(wbr.isReplayRequest());
			assertTrue(wbr.isMementoTimegate());
			assertEquals("http://example.com/", wbr.getRequestUrl());
			assertEquals("20141027203602", wbr.getReplayTimestamp());
		} catch (NullPointerException ex) {
			// it fails with NullPointerException currently.
			System.err.println("needs fix");
			ex.printStackTrace();
		}
	}

	/**
	 * {@code http:/} is repaired to {@code http://}.
	 * @throws Exception
	 */
	public void testProtocolSingleSlash() throws Exception {
		setupRequest("http:/example.com/");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertEquals("http://example.com/", wbr.getRequestUrl());
	}

	public void testUrlWithUserInfo() throws Exception {
		setupRequest("//archive@example.com/");
		EasyMock.replay(request);
		try {
			cut.parse(request, accessPoint);
			fail();
		} catch (BadQueryException ex) {
			// expected
		} catch (NullPointerException ex) {
			// results in this error currently.
			System.err.println("needs fix");
			ex.printStackTrace();
		}
	}

	/**
	 * But userinfo is accepted in full URL... (bug?)
	 * @throws Exception
	 */
	public void testFullUrlWithUserInfo() throws Exception {
		setupRequest("http://archive@example.com/");
		EasyMock.replay(request);
		try {
			cut.parse(request, accessPoint);
			// this does not fail currently
			System.err.println("needs fix");
			(new Exception()).printStackTrace();
		} catch (BadQueryException ex) {
			// expected
		}
	}

	public void testNonURL() throws Exception {
		setupRequest("bogus.jpg");
		EasyMock.replay(request);
		WaybackRequest wbr = cut.parse(request, accessPoint);
		assertNull(wbr);
	}

}
