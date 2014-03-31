/**
 * 
 */
package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.writer.CDXWriter;
import org.archive.format.cdx.CDXFieldConstants;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.url.KeyMakerUrlCanonicalizer;
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
		FieldSplitFormat fmt = CDXFieldConstants.CDX_ALL_NAMES;
		testCDXServer.cdxLines = new CDXLine[] {
				new CDXLine(CDXLINE2, fmt)
		};
		
		SearchResults sr = cut.query(wbr);
		
		assertEquals(1, testCDXServer.capturedArgs.size());
		
		Object[] args = testCDXServer.capturedArgs.get(0);
		//CDXQuery query = (CDXQuery)args[0];
		AuthToken authToken = (AuthToken)args[1];
		assertTrue(authToken.isIgnoreRobots());
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
		
		assertEquals(CDXLINE1+"\n", sw.toString());
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
}
