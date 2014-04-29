package org.archive.wayback.archivalurl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import org.archive.wayback.archivalurl.requestparser.DatelessReplayRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.memento.MementoConstants;
import org.archive.wayback.memento.TimeGateBadQueryException;
import org.archive.wayback.webapp.AccessPoint;
import org.easymock.EasyMock;

/**
 * Test case for {@link ArchivalUrlRequestParser}.
 *
 * This class tests entire {@code ArchivalUrlRequestParser}, including its sub-component
 * classes: {@link ReplayRequestParser}, {@link PathDatePrefixQueryRequestParser}
 * {@link PathDateRangeQueryRequestParser}, {@link PathPrefixDatePrefixQueryRequestParser},
 * {@link PathPrefixDateRangeQueryRequestParser} and {@link DatelessReplayRequestParser}.
 * 
 * @author Kenji Nagahashi
 *
 */
public class ArchivalUrlRequestParserTest extends TestCase {
    ArchivalUrlRequestParser cut;
    AccessPoint accessPoint;
    
    // must set before calling parse().
    String acceptTimestampHeader = null;
    String acceptDatetimeHeader = null;
    
    final String EARLIEST_TIMESTAMP = "1996";
    final String EXPECTED_START_TIMESTAMP = "19960101000000";
    final String EXPECTED_END_TIMESTAMP = null;
    
    protected void setUp() throws Exception {
        super.setUp();
        cut = new ArchivalUrlRequestParser();
        // this affects what WaybackRequest.getStartTimestamp() returns.
        // here we leave lastestTimestamp null.
        cut.setEarliestTimestamp(EARLIEST_TIMESTAMP);
        // this needs to be called before use (but I don't see it called in
        // Spring config.)
        cut.init();
        // refactoring note: RequestParser#parser takes AccessPoint as an argument.
        // it is bad because AccessPoint is not mock-able and it is a big class.
        // it'd be great if we can cut out an interface required for RequestParser
        // and use it in RequestParser instead.
        // (partial) list of AccessPoint methods used by RequestParser:
        // - translateRequestPathQuery(HttpServletRequest) (defined in RequestHandler)
        // - getMapParam(Map, String) (static)
        // - isEnableMemento()
        accessPoint = new AccessPoint();
    }
    
    protected HttpServletRequest getRequestMock(String requestURI, String query) {
        HttpServletRequest mock = EasyMock.createNiceMock(HttpServletRequest.class);
        // DatelessReplayRequestParser checks out Accept-Datetime HTTP header. If we
        // want to test it, we need to setup return value for getHeader() method.
        // RequestMapper accesses HttpServletequest.getAttribute(RequestMapper.REQUEST_CONTEXT_PREFIX)
        // which can be null. so we leave it null.
        // prefix must end with "/" (see RequestMapper#handleRequest(HttpServletRequest, HttpServletResponse)
        EasyMock.expect(mock.getAttribute(EasyMock.eq("webapp-request-context-path-prefix"))).andStubReturn("/web/");
        EasyMock.expect(mock.getRequestURI()).andStubReturn(requestURI);
        EasyMock.expect(mock.getQueryString()).andStubReturn(query);
        EasyMock.expect(mock.getHeader(MementoConstants.ACCEPT_DATETIME)).andStubReturn(acceptDatetimeHeader);
        EasyMock.expect(mock.getHeader("Accept-Timestamp")).andStubReturn(acceptTimestampHeader);
        // used by OpenSearchRequestParser
        EasyMock.expect(mock.getParameterMap()).andStubReturn(Collections.EMPTY_MAP);
        return mock;
    }

    protected WaybackRequest parse(String url) throws BadQueryException, BetterRequestException {
	    // note: url is set to HttpServletRequest's requestURI property. it should NOT contain
		// query part. RequestParser will receive value translated by RequestMapper.getRequestContextPath()
		// (i.e. "/web/" part removed.)
		HttpServletRequest req = getRequestMock(url, null);
		EasyMock.replay(req);
		return cut.parse(req, accessPoint);
    }
    
    public void testReplayRequest() throws Exception {
        WaybackRequest wbr = parse("/web/20100101000000/http://www.yahoo.com/");
        assertNotNull(wbr);
        assertTrue(wbr.isReplayRequest());
        assertEquals("20100101000000", wbr.getReplayTimestamp());
        assertEquals("http://www.yahoo.com/", wbr.getRequestUrl());
    }
    
    /**
     * timestamp without "*", path ending with "*".
     * <p>this is interpreted as replay request for the URL including trailing "*".
     * That sounds inconsistent with other cases. (BTW, resultant time-redirect URL
     * has no trailing "*" - there's a special handling happening somewhere.)
     * Should this be interpreted as URL-query for the specific date?</p>
     * @throws Exception
     */
    public void testDatePathPrefix() throws Exception {
    	WaybackRequest wbr = parse("/web/20100101000000/http://www.yahoo.com/*");
    	assertNotNull(wbr);
    	assertTrue(wbr.isReplayRequest());
    	assertEquals("20100101000000", wbr.getReplayTimestamp());
    	// ReplayRequestParser does not strip trailing "*" off.
    	assertEquals("http://www.yahoo.com/*", wbr.getRequestUrl());
    }
    
    /**
     * test of {@link PathDatePreofixQueryRequestParser}.
     */ 
    public void testDatePrefix() throws Exception {
    	// less-than-14-digit timestamp with "*": narrowed time range, highlight
    	// the latest within the range.
    	WaybackRequest wbr1 = parse("/web/20100101*/http://www.yahoo.com/?p=2");
    	assertNotNull(wbr1);
    	assertTrue(wbr1.isCaptureQueryRequest());
    	assertEquals("20100101000000", wbr1.getStartTimestamp());
    	assertEquals("20100101235959", wbr1.getEndTimestamp());
    	assertEquals("20100101235959", wbr1.getReplayTimestamp());
    	assertEquals("http://www.yahoo.com/?p=2", wbr1.getRequestUrl());
    	
    	// just "*": entire time range, replay the latest.
    	WaybackRequest wbr2 = parse("/web/*/http://www.yahoo.com/");
    	assertNotNull(wbr2);
    	assertTrue(wbr2.isCaptureQueryRequest());
    	assertEquals(EXPECTED_START_TIMESTAMP, wbr2.getStartTimestamp());
    	assertEquals(EXPECTED_END_TIMESTAMP, wbr2.getEndTimestamp());
    	assertEquals(null, wbr2.getReplayTimestamp());
    	
    	// full 14-digit timestamp with "*": entire time range, highlight the 
    	// closest to the specified date.
    	WaybackRequest wbr3 = parse("/web/20100101000000*/http://www.yahoo.com/");
    	assertNotNull(wbr3);
    	assertTrue(wbr3.isCaptureQueryRequest());
    	assertEquals(EXPECTED_START_TIMESTAMP, wbr3.getStartTimestamp());
    	assertEquals(EXPECTED_END_TIMESTAMP, wbr3.getEndTimestamp());
    	assertEquals("20100101000000", wbr3.getReplayTimestamp());
    }
    
    /**
     * Another test of {@link PathDatePrefixQueryRequestParser}:
     * URL is recognized even when "{@code *}" is %-encoded.
     * <p>this is a desired behavior to be implemented (assertion is disabled).
     * see issue <a href="https://webarchive.jira.com/browse/WWM-110">WWM-110</a></p>
     * @throws Exception
     */
    public void testDatePrefixEncoded() throws Exception {
		WaybackRequest wbr1 = parse("/web/20100101%2A/http://www.yahoo.com/?p=%2A");
		assertNotNull(wbr1);
		assertTrue(wbr1.isCaptureQueryRequest());
		assertEquals("20100101000000", wbr1.getStartTimestamp());
		assertEquals("20100101235959", wbr1.getEndTimestamp());
		assertEquals("20100101235959", wbr1.getReplayTimestamp());
		assertEquals("http://www.yahoo.com/?p=%2A", wbr1.getRequestUrl());
    	
		WaybackRequest wbr2 = parse("/web/%2A/http://www.yahoo.com/");
		assertNotNull(wbr2);
		assertTrue(wbr2.isCaptureQueryRequest());
		assertEquals(EXPECTED_START_TIMESTAMP, wbr2.getStartTimestamp());
		assertEquals(EXPECTED_END_TIMESTAMP, wbr2.getEndTimestamp());
		assertEquals(null, wbr2.getReplayTimestamp());
    }
    
    /**
     * test for {@link PathDateRangeQueryRequestParser}.
     * <p>date range and specific path.
     * date range becomes start and end timestamps. but it doesn't
     * set replayTimestamp (it should, for consistency?)
     */
    public void testPathDateRange() throws Exception {
    	// range with full-length timestamps
		WaybackRequest wbr1 = parse("/web/20100101000000-20100630235959*/http://www.yahoo.com/");
		assertNotNull(wbr1);
		assertTrue(wbr1.isCaptureQueryRequest());
		assertEquals("20100101000000", wbr1.getStartTimestamp());
		assertEquals("20100630235959", wbr1.getEndTimestamp());
//		assertEquals("20100630235959", wbr1.getReplayTimestamp());
		assertEquals(null, wbr1.getReplayTimestamp());
		assertEquals("http://www.yahoo.com/", wbr1.getRequestUrl());
    	
		WaybackRequest wbr2 = parse("/web/2010-2014*/http://www.yahoo.com/?p=2");
		assertNotNull(wbr2);
		assertTrue(wbr1.isCaptureQueryRequest());
		assertEquals("20100101000000", wbr2.getStartTimestamp());
		assertEquals("20141231235959", wbr2.getEndTimestamp());
//		assertEquals("20141231235959", wbr2.getReplayTimestamp());
		assertEquals(null, wbr2.getReplayTimestamp());
		assertEquals("http://www.yahoo.com/?p=2", wbr2.getRequestUrl());
		
		// Date range without "*" results in 404. We could make it work.
		WaybackRequest wbr3 = parse("/web/2010-2014/http://www.yahoo.com/");
		assertNull(wbr3);
    }

    /**
     * test for {@link PathDateRangeQueryRequestParser}, %-encoded version.
     * @throws Exception
     */
    public void testPathDateRangeEncoded() throws Exception {
		WaybackRequest wbr1 = parse("/web/20100101000000-20100630235959%2A/http://www.yahoo.com/");
		assertNotNull(wbr1);
		assertTrue(wbr1.isCaptureQueryRequest());
		assertEquals("20100101000000", wbr1.getStartTimestamp());
		assertEquals("20100630235959", wbr1.getEndTimestamp());
//		assertEquals("20100630235959", wbr1.getReplayTimestamp());
		assertEquals(null, wbr1.getReplayTimestamp());
		assertEquals("http://www.yahoo.com/", wbr1.getRequestUrl());
    }

    /**
     * test of {@link PathPrefixDatePrefixQueryRequestParser}.
     * <p>this is a URL query with (optional) single timestamp date range.
     * timestamp, if non-empty, becomes start and end.
     * </p>
     */
    public void testPathPrefixDatePrefix() throws Exception {
    	WaybackRequest wbr1 = parse("/web/2010*/http://www.yahoo.com/*");
    	assertNotNull(wbr1);
    	assertTrue(wbr1.isUrlQueryRequest());
    	assertEquals("20100101000000", wbr1.getStartTimestamp());
    	assertEquals("20101231235959", wbr1.getEndTimestamp());
    	// does not set replayTimestamp, but it is not a required behavior.
    	assertEquals("http://www.yahoo.com/", wbr1.getRequestUrl());
    	
    	WaybackRequest wbr2 = parse("/web/*/http://www.yahoo.com/*");
    	assertNotNull(wbr2);
    	assertTrue(wbr2.isUrlQueryRequest());
    	assertEquals(EXPECTED_START_TIMESTAMP, wbr2.getStartTimestamp());
    	assertEquals(null, wbr2.getEndTimestamp());
    	// does not set replayTimestamp, but it is not a required behavior.
    	assertEquals("http://www.yahoo.com/", wbr2.getRequestUrl());
    	
    	// timestamp is up to 13 digits. 14-digit timestamp results in null (-> 404).
    	// TODO: there'd be a nicer way.
    	WaybackRequest wbr3 = parse("/web/20130101000000*/http://www.yahoo.com/*");
    	assertNull(wbr3);
    }
    /**
     * test of {@link PathPrefixDatePrefixQueryRequestParser}.
     * <p>%-encoded timestamp.</p>
     * @throws Exception
     */
    public void testPathPrefixDatePrefixEncoded() throws Exception {
		{
			WaybackRequest wbr = parse("/web/2010%2A/http://www.yahoo.com/*");
			assertNotNull(wbr);
			assertTrue(wbr.isUrlQueryRequest());
			assertEquals("20100101000000", wbr.getStartTimestamp());
			assertEquals("20101231235959", wbr.getEndTimestamp());
			assertEquals("http://www.yahoo.com/", wbr.getRequestUrl());
		}
		// negative case - %2A doesn't make it path-prefix.
		{
			WaybackRequest wbr = parse("/web/2010%2A/http://www.yahoo.com/%2A");
			assertNotNull(wbr);
			assertTrue(wbr.isCaptureQueryRequest());
			assertEquals("20100101000000", wbr.getStartTimestamp());
			assertEquals("20101231235959", wbr.getEndTimestamp());
			assertEquals("http://www.yahoo.com/%2A", wbr.getRequestUrl());
		}
    }

	/**
     * test of {@link PathPrefixDateRangeQueryRequestParser}.
     * <p>explicit timestamp range and trailing "*" in URL.
     * this is a URL-query request, timestamp range becomes 
     * start and end timestamp.</p>
     * <p>timerange without "*" is not recognized. it could be.</p> 
     */
	public void testPathPrefixDateRange() throws Exception {
		{
			WaybackRequest wbr1 = parse("/web/20100101-20100531*/http://www.yahoo.com/*");
			assertNotNull(wbr1);
			assertTrue(wbr1.isUrlQueryRequest());
			assertEquals("20100101000000", wbr1.getStartTimestamp());
			assertEquals("20100531235959", wbr1.getEndTimestamp());
			assertEquals("http://www.yahoo.com/", wbr1.getRequestUrl());
		}

		// TODO: date range without "*"
	}

	/**
	 * test of {@link PathPrefixDateRangeQueryRequestParser},
	 * %-encoded version.
	 * @throws Exception
	 */
	public void testPathPrefixdateRangeEncoded() throws Exception {
		{
			WaybackRequest wbr = parse("/web/20100101%2D20100531%2A/http://www.yahoo.com/*");
			assertNotNull(wbr);
			assertTrue(wbr.isUrlQueryRequest());
			assertEquals("20100101000000", wbr.getStartTimestamp());
			assertEquals("20100531235959", wbr.getEndTimestamp());
			assertEquals("http://www.yahoo.com/", wbr.getRequestUrl());
		}
	}
	
	protected void checkPathDateless(WaybackRequest wbr, String requestUrl) {
		assertNotNull(wbr);
		assertTrue(wbr.isReplayRequest());
		assertTrue(wbr.isBestLatestReplayRequest());
		assertEquals(EXPECTED_START_TIMESTAMP, wbr.getStartTimestamp());
		assertEquals(EXPECTED_END_TIMESTAMP, wbr.getEndTimestamp());
		assertEquals(requestUrl, wbr.getRequestUrl());
		assertNotNull(wbr.getReplayDate());
		assertNotNull(wbr.getAnchorDate());
	}

	/**
	 * test of {@link DatelessReplayRequestParser}.
	 * <p>It also reads Replay/AchorDate from Accept-Date/Accept-Timestamp
	 * HTTP headers (Accept-Date is part of Memento protocol). This test is
	 * included here, even though it has some Memento-related code, because
	 * core part of processing is for Archival-URL.</p>
	 * <p>One notable difference from with-timestamp request is that
	 * {@link WaybackRequest#isBestLatestReplayRequest} is set to true, which used
	 * in several places for unclear purpose. This flag is not set in other
	 * RequestParser's, even if request is asking for the best latest capture.
	 * Probably flag's semantics is slightly different from what its name suggests.</p>
	 * <p>As this pattern of requests come from URL that escaped Archival-URL
	 * rewriting process, it runs extra check on it. Test includes a few negative cases
	 * for non-URL paths.</p>
	 * @throws Exception
	 */
	public void testPathDateless() throws Exception {
		{
			WaybackRequest wbr = parse("/web/http://www.yahoo.com/");
			checkPathDateless(wbr, "http://www.yahoo.com/");
		}
		{
			WaybackRequest wbr = parse("/web/https://www.yahoo.com/");
			checkPathDateless(wbr, "https://www.yahoo.com/");
		}
		{
			WaybackRequest wbr = parse("/web/http://www.yahoo.com:8080/");
			checkPathDateless(wbr, "http://www.yahoo.com:8080/");
		}
		// some client canonicalizes "//" in path into "/".
		// (why this isn't done for other patterns of requests?)
		{
			WaybackRequest wbr = parse("/web/http:/www.yahoo.com/");
			checkPathDateless(wbr, "http://www.yahoo.com/");
		}
		// doesn't repair "https:/"
		{
			WaybackRequest wbr = parse("/web/https:/www.yahoo.com/");
			//checkPathDateless(wbr, "https://www.yahoo.com/");
			assertNull(wbr);
		}
		// doesn't repair "ftp:/" either.
		{
			WaybackRequest wbr = parse("/web/ftp:/www.yahoo.com/afile");
			assertNull(wbr);
		}
		// scheme-relative - results in NullPointerException FIXME
		{
			try {
				WaybackRequest wbr = parse("/web///www.yahoo.com/");
				assertNull(wbr);
			} catch (NullPointerException ex) {
				// current behavior - FIXME.
			}
		}
		// regular case.
		{
			WaybackRequest wbr = parse("/web/www.yahoo.com/");
			checkPathDateless(wbr, "http://www.yahoo.com/");
		}
		// scheme-less URL with user info is rejected
		// TODO: why is this rejected?
		{
			try {
				@SuppressWarnings("unused")
				WaybackRequest wbr = parse("/web/user@www.yahoo.com/");
				fail("BadQueryException was not thrown");
			} catch (BadQueryException ex) {
				// expected;
			}
		}
		// but it's accepted with scheme.
		// TODO: should this be rejected as well?
		{
			WaybackRequest wbr = parse("/web/http://user@www.yahoo.com/");
			checkPathDateless(wbr, "http://user@www.yahoo.com/");
		}
		// just make sure path and query parts in requestUrl are preserved.
		{
			WaybackRequest wbr = parse("/web/www.yahoo.com/apis?v=2");
			checkPathDateless(wbr, "http://www.yahoo.com/apis?v=2");
		}
		// doesn't look like an URL.
		{
			WaybackRequest wbr = parse("/web/images/foo.gif");
			assertNull(wbr);
		}
		{
			WaybackRequest wbr = parse("/web/handler.php?url=http://www.yahoo.com/");
			assertNull(wbr);
		}
		// TODO: shouldn't this be parsed as dateless URL-Query?
		{
			WaybackRequest wbr = parse("/web/http://www.yahoo.com/*");
			checkPathDateless(wbr, "http://www.yahoo.com/*");
		}
		// ditto
		{
			WaybackRequest wbr = parse("/web/www.yahoo.com/*");
			checkPathDateless(wbr, "http://www.yahoo.com/*");
		}
	}
	
	public void testPathDatelessWithDateHeader() throws Exception {
		final String dateHeader = "Thu, 24 Apr 2014 21:15:51 GMT";
		final Date date = (new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z")).parse(dateHeader);
		{
			acceptDatetimeHeader = dateHeader;
			WaybackRequest wbr = parse("/web/http://www.yahoo.com/");
			assertNotNull(wbr);
			assertTrue(wbr.isReplayRequest());
			assertFalse(wbr.isBestLatestReplayRequest());
			assertEquals(EXPECTED_START_TIMESTAMP, wbr.getStartTimestamp());
			assertEquals(EXPECTED_END_TIMESTAMP, wbr.getEndTimestamp());
			assertEquals(date, wbr.getReplayDate());
			assertEquals(date, wbr.getAnchorDate());
		}
		// invalid Accept-Datetime header
		{
			acceptDatetimeHeader = "invalid date";
			try {
				@SuppressWarnings("unused")
				WaybackRequest wbr = parse("/web/http://www.yahoo.com/");
				fail("did not throw exception");
			} catch (TimeGateBadQueryException ex) {
				// expected
			}
		}
		// alternate Accept-Timestamp header. as long as it is valid,
		// invalid value in Accept-Datetime header doesn't cause exception.
		{
			acceptDatetimeHeader = "invalid date";
			acceptTimestampHeader = "20140424211551";
			WaybackRequest wbr = parse("/web/http://www.yahoo.com/");
			assertNotNull(wbr);
			assertTrue(wbr.isReplayRequest());
			assertFalse(wbr.isBestLatestReplayRequest());
			assertEquals(EXPECTED_START_TIMESTAMP, wbr.getStartTimestamp());
			assertEquals(EXPECTED_END_TIMESTAMP, wbr.getEndTimestamp());
			assertEquals(date, wbr.getReplayDate());
			assertEquals(date, wbr.getAnchorDate());
		}
		// invalid value in Accept-Timestamp header is silently ignored,
		// unless Accept-Datetime also has an invalid value. 
		{
			acceptDatetimeHeader = null;
			acceptTimestampHeader = "*INVALID*";
			WaybackRequest wbr = parse("/web/http://www.yahoo.com/");
			assertNotNull(wbr);
			assertTrue(wbr.isReplayRequest());
			assertTrue(wbr.isBestLatestReplayRequest());
			assertEquals(EXPECTED_START_TIMESTAMP, wbr.getStartTimestamp());
			assertEquals(EXPECTED_END_TIMESTAMP, wbr.getEndTimestamp());
		}
	}

	/**
	 * some pathological cases.
	 * @throws Exception
	 */
    public void testPathological() throws Exception {
		{
			WaybackRequest wbr = parse("/web/20100101*30/http://www.yahoo.com/?p=*");
			assertNull(wbr);
		}
		{
			WaybackRequest wbr = parse("/web/*20100101*/http://www.yahoo.com/");
			assertNull(wbr);
		}
		{
			WaybackRequest wbr = parse("/web/20100101*im_/http://www.yahoo.com/a.png");
			assertNull(wbr);
		}
		// TODO: should we accept this?
		{
			WaybackRequest wbr = parse("/web//20100101*/http://www.yahoo.com/");
			assertNull(wbr);
		}
    }
}
