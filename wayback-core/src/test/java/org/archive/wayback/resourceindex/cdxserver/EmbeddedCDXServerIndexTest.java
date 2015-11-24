/**
 * 
 */
package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.auth.PrivTokenAuthChecker;
import org.archive.cdxserver.writer.CDXWriter;
import org.archive.cdxserver.writer.HttpCDXWriter;
import org.archive.format.cdx.CDXFieldConstants;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.format.gzip.zipnum.ZipNumCluster;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.accesscontrol.robotstxt.redis.RedisRobotExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.WrappedCloseableIterator;
import org.archive.wayback.util.url.KeyMakerUrlCanonicalizer;
import org.archive.wayback.webapp.PerfStats;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test {@link EmbeddedCDXServerIndex}.
 * @author Kenji Nagahashi
 *
 */
public class EmbeddedCDXServerIndexTest extends TestCase {
	
	/**
	 * fixture CDXServer (unnecessary if CDServer was an interface).
	 * <p>
	 * Note: {@code testHandleRequest} and {@code testRenderMementoTimemap} uses
	 * {@link CDXServer#getCdx(HttpServletRequest, HttpServletResponse, CDXQuery)},
	 * which eventually calls {@link #getCdx(CDXQuery, AuthToken, CDXWriter)} here.
	 * </p>
	 */
	public static class TestCDXServer extends CDXServer {
		public List<Object[]> capturedArgs = new ArrayList<Object[]>();
		public CDXLine[] cdxLines;
		
		@Override
		public void getCdx(CDXQuery query, AuthToken authToken,
				CDXWriter responseWriter) throws IOException {
			capturedArgs.add(new Object[] { query, authToken, responseWriter });
			
			responseWriter.begin();
			for (CDXLine cdxLine : cdxLines) {
				responseWriter.writeLine(cdxLine);
			}
			responseWriter.end();
		}
		
		public void clearCapturedArgs() {
			capturedArgs.clear();
		}
	}

	EmbeddedCDXServerIndex cut;
	TestCDXServer testCDXServer;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		cut = new EmbeddedCDXServerIndex();
		cut.setCanonicalizer(new KeyMakerUrlCanonicalizer());
		cut.setCdxServer(testCDXServer = new TestCDXServer());

		Logger.getLogger(PerfStats.class.getName()).setLevel(Level.WARNING);
	}
	
	/**
	 * Set CDX lines TestCDXServer stub returns.
	 * Lines are parsed with {@link CDXFieldConstants#CDX_ALL_NAMES}.
	 * Note {@link EmbeddedCDXServerIndex#query} will throw {@link ResourceNotInArchiveException}
	 * if {@code lines} is empty.
	 * @param lines text CDX lines
	 */
	protected void setCdxLines(String... lines) {
		// urlkey, timestamp, original, mimetype, statuscode, digest, redirect,
		// robotflags, length, offset, filename.
		final FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		testCDXServer.cdxLines = new CDXLine[lines.length];
		int i = 0;
		for (String line : lines) {
			testCDXServer.cdxLines[i++] = new CDXLine(line, fmt);
		}
	}

	// === sample cdx lines ===
	
	final String CDXLINE1 = "com,example)/ 20101124000000 http://example.com/ text/html 200" +
			" ABCDEFGHIJKLMNOPQRSTUVWXYZ012345 - - 2000 0 /a/a.warc.gz";
	// for testing ignore-robots
	final String CDXLINE2 = "com,norobots)/ 20101124000000 http://example.com/ text/html 200" +
			" ABCDEFGHIJKLMNOPQRSTUVWXYZ012345 - - 2000 0 /a/a.warc.gz";
	/**
	 * capture search. basic options.
	 * @throws Exception
	 */
	public void testQuery() throws Exception {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setRequestUrl("http://example.com/");
		wbr.setCaptureQueryRequest();
		
		// urlkey, timestamp, original, mimetype, statuscode, digest, redirect, robotflags,
		// length, offset, filename.
		FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		testCDXServer.cdxLines = new CDXLine[] {
				new CDXLine(CDXLINE1, fmt)
		};
		
		SearchResults sr = cut.query(wbr);
		
		assertEquals(1,  sr.getReturnedCount());
		
		assertEquals(1, testCDXServer.capturedArgs.size());
		
		Object[] args = testCDXServer.capturedArgs.get(0);
		CDXQuery query = (CDXQuery)args[0];
		String[] filter = query.getFilter();
		assertEquals(1, filter.length);
		assertEquals("!statuscode:(500|502|504)", filter[0]);
		
		AuthToken authToken = (AuthToken)args[1];
		assertFalse(authToken.isIgnoreRobots());
	}
	/**
	 * {@link EmbeddedCDXServerIndex} resolves revisits for replay requests.
	 * (This is actually a test of {@link CDXToCaptureSearchResultsWriter}.)
	 * @throws Exception
	 */
	public void testRevisitResolution() throws Exception {
		WaybackRequest wbr = WaybackRequest.createReplayRequest(
			"http://example.com/", "20101125000000", null, null);
		setCdxLines(
			"com,example)/ 20101124000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/a.warc.gz",
			"com,example)/ 20101125000000 http://example.com/ warc/revisit 200" +
					" XXXX - - 2000 0 /a/b.warc.gz",
			"com,example)/ 20101126000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/c.warc.gz"
				);
		SearchResults sr = cut.query(wbr);

		assertEquals(3, sr.getReturnedCount());

		CaptureSearchResults results = (CaptureSearchResults)sr;
		List<CaptureSearchResult> list = results.getResults();
		CaptureSearchResult capture2 = list.get(1);
		assertEquals("20101125000000", capture2.getCaptureTimestamp());
		assertEquals("20101124000000", capture2.getDuplicateDigestStoredTimestamp());
		assertEquals("/a/a.warc.gz", capture2.getDuplicatePayloadFile());
		assertEquals(0, (long)capture2.getDuplicatePayloadOffset());
		assertEquals(2000, capture2.getDuplicatePayloadCompressedLength());

		assertSame(list.get(0), capture2.getDuplicatePayload());
	}

	/**
	 * {@link CDXToCaptureSearchResultsWriter} resolves revisits for replay requests
	 * (reverse order input mode) (Test of {@link CDXToCaptureSearchResultsWriter}.)
	 * <p>Since there's no way to put {@code CDXToCaptureSearchResultsWriter}'s in reverse
	 * mode, this test calls {@code CDXToCaptureSearchResultWriter} directly.</p>
	 * <p>In other words, its reverse mode is never used in practice.</p>
	 * @throws Exception
	 */
	public void testRevisitResolutionReverse() throws Exception {
		WaybackRequest wbr = WaybackRequest.createReplayRequest(
			"http://example.com/", "20101125000000", null, null);
		final String[] CDXLINES = {
			"com,example)/ 20101124000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/a.warc.gz",
			"com,example)/ 20101125000000 http://example.com/ warc/revisit 200" +
					" XXXX - - 2000 0 /a/b.warc.gz",
			"com,example)/ 20101126000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/c.warc.gz"
		};
		CDXQuery query = new CDXQuery(wbr.getRequestUrl());
		query.setSort(CDXQuery.SortType.reverse);
		assertTrue(query.isReverse());
		CDXToCaptureSearchResultsWriter cdxw = new CDXToCaptureSearchResultsWriter(query, true, false, null);

		final FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		cdxw.begin();
		// feed in reverse order
		for (int i = CDXLINES.length; i > 0; i--) {
			CDXLine line = new CDXLine(CDXLINES[i - 1], fmt);
			cdxw.trackLine(line);
			cdxw.writeLine(line);
		}
		cdxw.end();

		CaptureSearchResults results = cdxw.getSearchResults();

		assertEquals(3, results.getReturnedCount());

		List<CaptureSearchResult> list = results.getResults();

		CaptureSearchResult capture1 = list.get(0);
		// CDXToCaptureSearchResultWriter returns CaptureSearchResult's in chronological
		// order (oldest to newer), even when query.isReverse() == true.
		assertEquals("20101124000000", capture1.getCaptureTimestamp());

		CaptureSearchResult capture2 = list.get(1);
		assertEquals("20101125000000", capture2.getCaptureTimestamp());
		assertEquals("20101124000000", capture2.getDuplicateDigestStoredTimestamp());
		assertEquals("/a/a.warc.gz", capture2.getDuplicatePayloadFile());
		assertEquals(0, (long)capture2.getDuplicatePayloadOffset());
		assertEquals(2000, capture2.getDuplicatePayloadCompressedLength());

		assertSame(capture1, capture2.getDuplicatePayload());
	}

	/**
	 * Test of soft-block feature (regular replay).
	 * capture with "X" in {@code robotflags} field does not make its way
	 * into {@code CaptureSearchResults}, but still available as payload
	 * capture for revisits.
	 * @throws Exception
	 */
	public void testSoftBlock() throws Exception {
		WaybackRequest wbr = WaybackRequest.createReplayRequest(
			"http://example.com/", "20101125000000", null, null);
		setCdxLines(
			"com,example)/ 20101124000000 http://example.com/ text/html 200" +
					" XXXX - X 2000 0 /a/a.warc.gz",
			"com,example)/ 20101125000000 http://example.com/ warc/revisit 200" +
					" XXXX - - 2000 0 /a/b.warc.gz",
			"com,example)/ 20101126000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/c.warc.gz"
				);
		CaptureSearchResults results = (CaptureSearchResults)cut.query(wbr);

		assertEquals(2, results.getReturnedCount());

		// first line is excluded
		List<CaptureSearchResult> list = results.getResults();
		assertEquals(2, list.size());

		CaptureSearchResult capture1 = list.get(0);
		assertEquals("20101125000000", capture1.getCaptureTimestamp());

		CaptureSearchResult capture2 = list.get(1);
		assertEquals("20101126000000", capture2.getCaptureTimestamp());

		// but revisit is resolved to the first line.
		assertEquals("20101124000000", capture1.getDuplicateDigestStoredTimestamp());
		assertEquals("/a/a.warc.gz", capture1.getDuplicatePayloadFile());
		assertEquals(0, (long)capture1.getDuplicatePayloadOffset());
		assertEquals(2000, capture1.getDuplicatePayloadCompressedLength());

		// payload capture is available via duplicatePayload
		CaptureSearchResult captureX = capture1.getDuplicatePayload();
		assertNotNull(captureX);
		assertEquals("20101124000000", captureX.getCaptureTimestamp());

		// test if capture1 pretends to be an ordinary capture.
		// we want to hide the fact that it's content is coming from
		// blocked capture (this is actually a test of CaptereSearchResult.)
		assertFalse(capture1.isDuplicateDigest());
}

	/**
	 * Supplementary test for soft-block feature.
	 * Modification to {@code robotflags} made by {@code exclusionFilter} must be
	 * properly recognized. As baseline {@code EmbeddedCDXServerIndex} does not have
	 * setting up {@code exclusionFilter}, this test deals with
	 * {@link CDXToCaptureSearchResultsWriter} directly.
	 * @throws Exception
	 */
	public void testSoftBlock_fieldModificationRecognized() throws Exception {
		WaybackRequest wbr = WaybackRequest.createReplayRequest(
			"http://example.com/", "20101125000000", null, null);
		final String[] CDXLINES = {
			// note this line has no "X" in robotflags field (compare with test above)
			"com,example)/ 20101124000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/a.warc.gz",
			"com,example)/ 20101125000000 http://example.com/ warc/revisit 200" +
					" XXXX - - 2000 0 /a/b.warc.gz",
			"com,example)/ 20101126000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/c.warc.gz"
		};
		CDXQuery query = new CDXQuery(wbr.getRequestUrl());
		ExclusionFilter exclusionFilter = new ExclusionFilter() {
			@Override
			public int filterObject(CaptureSearchResult o) {
				if (o.getCaptureTimestamp().startsWith("20101124")) {
					o.setRobotFlag(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED);
				}
				return FILTER_INCLUDE;
			}
		};
		CDXToCaptureSearchResultsWriter cdxw = new CDXToCaptureSearchResultsWriter(query, true, false, null);
		cdxw.setExclusionFilter(exclusionFilter);

		final FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		cdxw.begin();
		for (String l : CDXLINES) {
			CDXLine line = new CDXLine(l, fmt);
			cdxw.trackLine(line);
			cdxw.writeLine(line);
		}
		cdxw.end();

		CaptureSearchResults results = cdxw.getSearchResults();

		// first capture will be removed from the result.
		assertEquals(2, results.getReturnedCount());

		List<CaptureSearchResult> list = results.getResults();

		CaptureSearchResult capture1 = list.get(0);

		assertEquals("20101125000000", capture1.getCaptureTimestamp());

		CaptureSearchResult captureX = capture1.getDuplicatePayload();
		assertNotNull(captureX);
		assertEquals("20101124000000", captureX.getCaptureTimestamp());
		// modification to robotflags field made by ExclusionFilter must be reflected
		// in capture1.
		assertEquals("X", captureX.getRobotFlags());
	}

	/**
	 * Test of soft-block feature (URL-agnostic revisit payload lookup).
	 * In revisit payload lookup mode, capture with "X" is returned.
	 * @throws Exception
	 */
	public void testSoftBlock_revisitPayloadLookup() throws Exception {
		WaybackRequest wbr = WaybackRequest.createReplayRequest(
			"http://example.com/", "20101124000000", null, null);
		wbr.put(EmbeddedCDXServerIndex.REQUEST_REVISIT_LOOKUP, "true");
		setCdxLines(
			"com,example)/ 20101124000000 http://example.com/ text/html 200" +
					" XXXX - X 2000 0 /a/a.warc.gz",
			"com,example)/ 20101125000000 http://example.com/ warc/revisit 200" +
					" XXXX - - 2000 0 /a/b.warc.gz",
			"com,example)/ 20101126000000 http://example.com/ text/html 200" +
					" XXXX - - 2000 0 /a/c.warc.gz"
				);
		CaptureSearchResults results = (CaptureSearchResults)cut.query(wbr);

		CaptureSearchResult capture1 = results.getResults().get(0);
		assertEquals("20101124000000", capture1.getCaptureTimestamp());
		assertSame(capture1, results.getClosest());
	}

	/**
	 * quick test of {@link EmbeddedCDXServerIndex#buildStatusFilter(String)}
	 */
	public void testBuildStatusFilter() {
		final String[][] CASES = new String[][] {
				{ "!500", "!statuscode:500" },
				{ "! 400|500|502 ", "!statuscode:400|500|502" },
				{ "[23]..", "statuscode:[23].." },
				{ "! ", "" },
				{ "", "" },
				{ null, "" }
		};
		for (String[] c : CASES) {
			assertEquals(c[1], EmbeddedCDXServerIndex.buildStatusFilter(c[0]));
		}
	}
	
	/**
	 * test of {@link EmbeddedCDXServerIndex#setBaseStatusRegexp(String)}
	 * @throws Exception
	 */
	public void testQueryWithCustomStatusFilter() throws Exception {
		WaybackRequest wbr = new WaybackRequest();
		wbr.setRequestUrl("http://example.com/");
		wbr.setCaptureQueryRequest();
		
		// urlkey, timestamp, original, mimetype, statuscode, digest, redirect, robotflags,
		// length, offset, filename.
		setCdxLines(CDXLINE1);
		
		cut.setBaseStatusRegexp("");
		{
			@SuppressWarnings("unused")
			SearchResults sr = cut.query(wbr);

			assertEquals(1, testCDXServer.capturedArgs.size());

			Object[] args = testCDXServer.capturedArgs.get(0);
			CDXQuery query = (CDXQuery)args[0];
			String[] filter = query.getFilter();
			assertNull("there should be no filter", filter);
		}
		
		testCDXServer.clearCapturedArgs();
		cut.setBaseStatusRegexp("!500");
		{
			@SuppressWarnings("unused")
			SearchResults sr = cut.query(wbr);

			assertEquals(1, testCDXServer.capturedArgs.size());
			
			Object[] args = testCDXServer.capturedArgs.get(0);
			CDXQuery query = (CDXQuery)args[0];
			String[] filter = query.getFilter();
			assertEquals(1, filter.length);
			assertEquals("!statuscode:500", filter[0]);
		}
	}
	
	/**
	 * for those SURT prefixes in {@code ignoreRobotsPaths}, 
	 * {@link AuthToken#isIgnoreRobots()} flag is set.
	 * @throws Exception
	 */
	public void testIgnoreRobotPaths() throws Exception {
		cut.setIgnoreRobotPaths(Arrays.asList(new String[]{ "com,norobots" }));
		WaybackRequest wbr = new WaybackRequest();
		wbr.setRequestUrl("http://norobots.com/");
		wbr.setCaptureQueryRequest();
		
		// urlkey, timestamp, original, mimetype, statuscode, digest, redirect, robotflags,
		// length, offset, filename.
		setCdxLines(CDXLINE2);
		
		@SuppressWarnings("unused")
		SearchResults sr = cut.query(wbr);
		
		assertEquals(1, testCDXServer.capturedArgs.size());
		
		Object[] args = testCDXServer.capturedArgs.get(0);
		//CDXQuery query = (CDXQuery)args[0];
		AuthToken authToken = (AuthToken)args[1];
		assertTrue(authToken.isIgnoreRobots());
	}
	
	/**
	 * test of timestamp-collapsing.
	 * <p>Actual processing happens in {@link CDXServer}. {@link EmbeddedCDXServerIndex}
	 * simply passes {@link WaybackRequest#getCollapseTime()} to {@link CDXQuery#setCollapse(String[])}.
	 * if {@code collapseTime} is unspecified in {@code WaybackRequest} (-1), default value
	 * {@code timestampDedupLength} will be used.
	 * @throws Exception
	 */
	public void testCollapseTime() throws Exception {
		WaybackRequest wbr = WaybackRequest.createCaptureQueryRequet(
			"http://example.com/", null, null, null);
		setCdxLines(CDXLINE1);

		{
			cut.setTimestampDedupLength(10);
			@SuppressWarnings("unused")
			SearchResults sr = cut.query(wbr);

			Object[] args = testCDXServer.capturedArgs.get(0);
			assertEquals(10, ((CDXQuery)args[0]).getCollapseTime());
		}
		testCDXServer.clearCapturedArgs();
		{
			wbr.setCollapseTime(8);
			@SuppressWarnings("unused")
			SearchResults sr = cut.query(wbr);

			Object[] args = testCDXServer.capturedArgs.get(0);
			assertEquals(8, ((CDXQuery)args[0]).getCollapseTime());
		}
	}

	/**
	 * {@link EmbeddedCDXServerIndex#handleRequest(HttpServletRequest, HttpServletResponse)} is
	 * a entry point for CDXServer API. It should return all accessible cdx lines, without applying
	 * any additional filters not requested by API user.
	 * @throws Exception
	 */
	public void testHandleRequest() throws Exception {
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getParameter("url")).andStubReturn("http://example.com/");
		
		HttpServletResponse response = EasyMock.createNiceMock(HttpServletResponse.class);
		StringWriter sw = new StringWriter();
		EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(sw));
		
		FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		testCDXServer.cdxLines = new CDXLine[] {
				new CDXLine(CDXLINE1, fmt)
		};

		EasyMock.replay(request, response);
		cut.handleRequest(request, response);
		
		assertEquals(1, testCDXServer.capturedArgs.size());
		Object[] args = testCDXServer.capturedArgs.get(0);
		
		CDXQuery query = (CDXQuery)args[0];
		assertEquals("API query should not have filter by default", 0, query.getFilter().length);

		assertEquals(String.format("%1$s%n", CDXLINE1), sw.toString());
	}

	/**
	 * {@link EmbeddedCDXServerIndex#renderMementoTimemap(WaybackRequest, HttpServletRequest, HttpServletResponse)}
	 * is a CDXServer API entry point for Memento format output.
	 * @throws Exception
	 */
	public void testRenderMementoTimemap() throws Exception {
		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		// Used in MementoLinkWriter
		EasyMock.expect(request.getRequestURL()).andAnswer(new IAnswer<StringBuffer>() {
			@Override
			public StringBuffer answer() throws Throwable {
				return new StringBuffer("/timemap/memento/http://example.com/");
			}
		});
		HttpServletResponse response = EasyMock.createNiceMock(HttpServletResponse.class);
		StringWriter sw = new StringWriter();
		EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(sw));
		
		// needs: 
		//   getMementoTimemapFormat() - passed to CDXQuery.output
		//   getRequestUrl() - passed to CDXQuery
		//   get(MementoConstants.PAGE_STARTS) (optional, passed to CDXQuery.from
		//   getAccessPoint() - if getMementoTimemapFormat() == MementoConstants.FORMAT_LINK,
		//     CDX is looked up by calling AccessPoint#queryIndex(WaybackRequest)
		WaybackRequest wbr = new WaybackRequest();
		wbr.setRequestUrl("http://example.com/");
		wbr.setMementoTimemapFormat("memento");
		
		FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		testCDXServer.cdxLines = new CDXLine[] {
				new CDXLine(CDXLINE1, fmt)
		};
		
		EasyMock.replay(request, response);
		boolean r = cut.renderMementoTimemap(wbr, request, response);
		
		assertTrue("renderMementoTimemap returns true", r);

		assertEquals(1, testCDXServer.capturedArgs.size());
		Object[] args = testCDXServer.capturedArgs.get(0);
		
		CDXQuery query = (CDXQuery)args[0];
		assertEquals("API query should not have filter by default", 0, query.getFilter().length);
		
		// Here we only check if output *looks like* Memento format. Detailed tests
		// shall be done by test case for MementoLinkWriter.
		//System.out.println("response=" + sw.toString());
		assertTrue(sw.toString().startsWith("<http://example.com/>;"));
	}

	// WaybackAuthChecker wants RedisRobotExclusionFilterFactory for
	// robotsExclusions. BAD, BAD, BAD!
	public static class ExcludeAllFilterFactory extends RedisRobotExclusionFilterFactory {
		@Override
		public ExclusionFilter get() {
			return new ExclusionFilter() {
				@Override
				public int filterObject(CaptureSearchResult o) {
					return ObjectFilter.FILTER_EXCLUDE;
				}
			};
		}
	}
	// XXX CDXServer demands ZipNumCluster even though it doesn't
	// call methods specific to it. BAD.
	public static class StubZipNumCluster extends ZipNumCluster {
		List<String> cdxlines;
		public StubZipNumCluster(String... cdxlines) {
			this.cdxlines = Arrays.asList(cdxlines);
		}
		// method called by EmbeddedCDXServer.query(WaybackRequest) for
		// non-paged queries.
		@Override
		public CloseableIterator<String> getCDXIterator(String key,
				String start, String end, ZipNumParams params)
				throws IOException {
			return new WrappedCloseableIterator<String>(cdxlines.iterator());
		}
	}
	/**
	 * robots.txt exclusion shall be disable for embeds.
	 * <p>TODO: This is actually testing classes in {@code wayback-cdx-server}
	 * module. Implemented here because it takes more work to do this
	 * in wayback-cdx-server module, and it makes little sense to do it before
	 * planned refactoring.</p>
	 * <p>Ref: WWM-119. A bug in {@link PrivTokenAuthChecker}.</p>
	 * @throws Exception
	 */
	public void testIgnoreRobotsForEmbeds() throws Exception {
		CDXServer cdxServer = new CDXServer();
		ZipNumCluster cdxSource = new StubZipNumCluster(
			"com,example)/style.css 20101124000000 http://example.com/style.css text/css 200"
					+ " ABCDEFGHIJKLMNOPQRSTUVWXYZ012345 - - 2000 0 /a/a.warc.gz");
		cdxServer.setZipnumSource(cdxSource);
		// This is the class being tested here... so AuthChecker shall no be mocked.
		// We cannot use PrivTokenAuthCheck class for this test, because it has no
		// real support for robots.txt exclusion. This is the main reason why we
		// cannot have this test in wayback-cdx-server project.
		WaybackAuthChecker authChecker = new WaybackAuthChecker();
		authChecker.setRobotsExclusions(new ExcludeAllFilterFactory());
		cdxServer.setAuthChecker(authChecker);
		cdxServer.afterPropertiesSet();
		cut.setCdxServer(cdxServer);

		{
			WaybackRequest wbRequest = WaybackRequest.createReplayRequest(
				"http://example.com/style.css", "20140101000000", null, null);
			wbRequest.setCSSContext(true); // i.e. "embed"

			try {
				cut.query(wbRequest);
			} catch (RobotAccessControlException ex) {
				fail("robots.txt exclusion is not disabled for embeds");
			}
		}
		// additional tests to make sure robots.txt exclusion is implemented
		// right, not just broken. these would have better been in a separate
		// test method(s), but just for now... CDX server refactoring will
		// break these anyways.
		{
			WaybackRequest wbRequest = WaybackRequest.createReplayRequest(
				"http://example.com/style.css", "20140101000000", null, null);
			// not embed
			try {
				cut.query(wbRequest);
				fail("RobotAccessControlException was not thrown");
			} catch (RobotAccessControlException ex) {
				// expected.
			}
		}

		// check robots.txt exclusion is working for CDX server API entry point
		{
			HttpServletRequest httpRequest = EasyMock.createNiceMock(HttpServletRequest.class);
			EasyMock.expect(httpRequest.getParameter("url")).andStubReturn("http://exmaple.com/style.css");

			HttpServletResponse httpResponse = EasyMock.createMock(HttpServletResponse.class);
			// expect error response; 403 with error header containing "Robot"
			final StringWriter output = new StringWriter();
			EasyMock.expect(httpResponse.getWriter()).andReturn(new PrintWriter(output));
			httpResponse.setContentType(EasyMock.<String>notNull());
			EasyMock.expectLastCall().once();
			httpResponse.setStatus(403);
			EasyMock.expectLastCall().once();
			httpResponse.setHeader(EasyMock.eq(HttpCDXWriter.RUNTIME_ERROR_HEADER), EasyMock.matches("(?i).*Robot.*"));

			EasyMock.replay(httpRequest, httpResponse);

			cut.handleRequest(httpRequest, httpResponse);

			EasyMock.verify(httpResponse);
		}

		// check if robots.txt exclusion can be disabled by cookie.
		{
			final String IGNORE_ROBOTS_TOKEN = "DISABLE-ROBOTS-EXCLUSION";
			authChecker.setIgnoreRobotsAccessTokens(Collections.singletonList(IGNORE_ROBOTS_TOKEN));

			HttpServletRequest httpRequest = EasyMock.createNiceMock(HttpServletRequest.class);
			EasyMock.expect(httpRequest.getParameter("url")).andStubReturn("http://exmaple.com/style.css");
			EasyMock.expect(httpRequest.getCookies()).andStubReturn(
				new Cookie[] { new Cookie(cdxServer.getCookieAuthToken(),
					IGNORE_ROBOTS_TOKEN) });

			HttpServletResponse httpResponse = EasyMock.createMock(HttpServletResponse.class);
			// expect 200 response = robots exclusion is disabled.
			final StringWriter output = new StringWriter();
			EasyMock.expect(httpResponse.getWriter()).andReturn(new PrintWriter(output));
			httpResponse.setContentType(EasyMock.<String>notNull());
			EasyMock.expectLastCall().once();
			//httpResponse.setStatus(200); // this is not explicitly called
			//EasyMock.expectLastCall().once();

			EasyMock.replay(httpRequest, httpResponse);

			cut.handleRequest(httpRequest, httpResponse);
			// if it's not working, EasyMock will report unexpected call to httpResponse.setStatus(403).

			EasyMock.verify(httpResponse);

			System.out.println(output.toString());
		}
	}
}
