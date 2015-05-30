/**
 * 
 */
package org.archive.wayback.replay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;

/**
 * unit test for {@link TransparentReplayRenderer}
 * 
 * @contributor kenji
 *
 */
public class TransparentReplayRendererTest extends TestCase {

    TransparentReplayRenderer cut;
    HttpServletRequest request;
    HttpServletResponse response;
    WaybackRequest wbRequest;
    CaptureSearchResult result = new CaptureSearchResult();
    ResultURIConverter uriConverter;
    // unused in TransparentReplayRenderer.
    CaptureSearchResults results = null;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        //HttpHeaderProcessor httpHeaderProcessor = new IdentityHttpHeaderProcessor();
        HttpHeaderProcessor httpHeaderProcessor = new RedirectRewritingHttpHeaderProcessor();
        cut = new TransparentReplayRenderer(httpHeaderProcessor);
        // unused in TransparentReplayRenderer
        wbRequest = null; //new WaybackRequest();
        // use test fixture version as we want to focus on TransparentReplayRenderer behavior.
        uriConverter = EasyMock.createMock(ResultURIConverter.class);
        // result is only used in HttpHeaderOperation.processHeaders()
        results = new CaptureSearchResults();
        
        request = EasyMock.createNiceMock(HttpServletRequest.class);
        response = EasyMock.createMock(HttpServletResponse.class);
    }

    public static class TestServletOutputStream extends ServletOutputStream {
        ByteArrayOutputStream out =  new ByteArrayOutputStream();
        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }
        
        public byte[] getBytes() {
            return out.toByteArray();
        }
        public String getString() {
            try {
                return out.toString("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("unexpected UnsupportedEncodingException", ex);
            }
        }
    }
    
    public void testRenderResource_BasicCapture() throws Exception {
        final String ct = "image/gif";
        WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(ct, TestWARCRecordInfo.PAYLOAD_GIF);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        Resource payloadResource = new WarcResource(rec, ar);
        payloadResource.parseHeaders();
        Resource headersResource = payloadResource;
        
        TestServletOutputStream servletOutput = new TestServletOutputStream();
        response.setStatus(200);
        EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
        response.setHeader("Content-Type", ct);
        // ??? RedirectRewritingHttpHeaderProcessor drops Content-Length header. is this really
        // it is supposed to do?
        //response.setHeader("Content-Length", Integer.toString(payloadBytes.length));
        response.setHeader(EasyMock.<String>notNull(), EasyMock.<String>notNull());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(response);

        cut.renderResource(request, response, wbRequest, result,
                headersResource, payloadResource, uriConverter, results);
        
        EasyMock.verify(response);
        byte[] content = servletOutput.getBytes();
        assertTrue("servlet output", Arrays.equals(TestWARCRecordInfo.PAYLOAD_GIF, content));
    }
    
    /**
     * test replay of capture with {@code Content-Encoding: gzip}.
     * TransparentReplayRenderer copies original, compressed payload to the output.
     * 
     * TODO: should render uncompressed content if client cannot handle
     * {@code Content-Encoding: gzip}.
     * 
     * @throws Exception
     */
    public void testRenderResource_CompressedCapture() throws Exception {
        final String ct = "image/gif";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(
                TestWARCRecordInfo.buildCompressedHttpResponseBlock(ct,
                        TestWARCRecordInfo.PAYLOAD_GIF));
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        Resource payloadResource = new WarcResource(rec, ar);
        payloadResource.parseHeaders();
        Resource headersResource = payloadResource;
        
        TestServletOutputStream servletOutput = new TestServletOutputStream();
        response.setStatus(200);
        EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
        response.setHeader("Content-Type", ct);
        response.setHeader("Content-Encoding", "gzip");
        // ??? RedirectRewritingHttpHeaderProcessor drops Content-Length header. is this really
        // what it is supposed to do?
        //response.setHeader("Content-Length", Integer.toString(payloadBytes.length));
        response.setHeader(EasyMock.<String>notNull(), EasyMock.<String>notNull());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(response);

        cut.renderResource(request, response, wbRequest, result,
                headersResource, payloadResource, uriConverter, results);
        
        EasyMock.verify(response);
        
        // content is the original gzip-compressed bytes for PAYLOAD_GIF.
        InputStream zis = new GZIPInputStream(new ByteArrayInputStream(servletOutput.getBytes()));
        byte[] content = new byte[TestWARCRecordInfo.PAYLOAD_GIF.length];
        zis.read(content);
        assertTrue("servlet output", Arrays.equals(TestWARCRecordInfo.PAYLOAD_GIF, content));
    }    
        
    public void testRenderResource_Redirect() throws Exception {
        String location = "http://www.example.com/index.html";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(TestWARCRecordInfo.buildHttpRedirectResponseBlock(location));
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        Resource payloadResource = new WarcResource(rec, ar);
        payloadResource.parseHeaders();
        
        final String originalUrl = "http://www.example.com/";
        final String captureTimestamp = "20130101123456";
        
        result.setOriginalUrl(originalUrl);
        result.setCaptureTimestamp(captureTimestamp);
        
        // makeReplayURI() is called through RedirectRewritingHttpHeaderProcessor.
        // TODO: perhaps HttpheaderProcessor is the right class to make fixture?
        EasyMock.expect(uriConverter.makeReplayURI(captureTimestamp, location))
        .andReturn("/web/" + captureTimestamp + "/" + location);

        TestServletOutputStream servletOutput = new TestServletOutputStream();
        response.setStatus(302);
        EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
        response.setHeader("Content-Type", "text/html");
        response.setHeader(EasyMock.eq("Location"), EasyMock.matches("/web/" + captureTimestamp + "/" + location));
        // RedirectRewritingHttpHeaderProcessor drops Content-Length.
        // response.setHeader("Content-Length", "0");
        response.setHeader(EasyMock.<String>notNull(), EasyMock.<String>notNull());
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.replay(response, uriConverter);

        cut.renderResource(request, response, wbRequest, result,
                payloadResource, payloadResource, uriConverter, results);
        
        EasyMock.verify(response, uriConverter);

        byte[] content = servletOutput.getBytes();
        assertEquals("payload length", 0, content.length);
    }

    /**
     * test replay of capture with {@code Transfer-Encoding: chunked}.
     *
     * <p>TransparentReplayRenderer writes out chunk-decoded payload, because
     * {@link WarcResource} always decodes chunked-entity. Point of this test
     * is that response never have {@code Transfer-Encoding: chunked} header,
     * even when initialized with {@link IdentityHttpHeaderProcessor}.
     * so, this is not really a unit test for TransparentReplayRenderer, but
     * a multi-component test placed here for convenience.</p>
     * <p>This test does not use member object {@code cut}, in order to test
     * with {@link IdentityHttpHeaderProcessor}.</p>
     *
     * @throws Exception
     */
    public void testRenderResource_Chunked() throws Exception {
        final String ct = "text/xml";
        final String payload = "<?xml version=\"1.0\"?>\n" +
        		"<payload name=\"archive\">\n" +
        		"  <inside/>\n" +
        		"</payload>\n";
        final byte[] recordBytes = TestWARCRecordInfo.buildHttpResponseBlock(
        		"200 OK", ct, payload.getBytes("UTF-8"), true);
        //System.out.println(new String(recordBytes, "UTF-8"));
        WARCRecordInfo recinfo = new TestWARCRecordInfo(recordBytes);
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = ar.get(0);
        Resource payloadResource = new WarcResource(rec, ar);
        payloadResource.parseHeaders();
        Resource headersResource = payloadResource;

        TestServletOutputStream servletOutput = new TestServletOutputStream();
        // expectations
        response.setStatus(200);
        EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
        // capture setHeader() call for "Transfer-Encoding"
        Capture<String> transferEncodingCapture = new Capture<String>(CaptureType.FIRST);
        response.setHeader(EasyMock.eq("Transfer-Encoding"), EasyMock.capture(transferEncodingCapture));
        EasyMock.expectLastCall().anyTimes();
        response.setHeader(EasyMock.<String>anyObject(), EasyMock.<String>anyObject());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(response);

        // creating separate test object to use IdentityHttpHeaderProcessor
        TransparentReplayRenderer cut2 = new TransparentReplayRenderer(new IdentityHttpHeaderProcessor());
        cut2.renderResource(request, response, wbRequest, result,
        		headersResource, payloadResource, uriConverter, results);

        EasyMock.verify(response);

        assertFalse("Transfer-Encoding header must not be set", transferEncodingCapture.hasCaptured());

        // content is the original gzip-compressed bytes for PAYLOAD_GIF.
        String output = new String(servletOutput.getBytes(), "UTF-8");
        assertEquals(payload, output);
    }

	protected byte[] buildTestBytes(int length) {
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = (byte)(i & 0xff);
		}
		return bytes;
	}

    public void testRenderResource_Range_From200() throws Exception {
		final String ct = "application/octet-stream";
		final byte[] payload = buildTestBytes(1024);
		final byte[] recordBytes = TestWARCRecordInfo.buildHttpResponseBlock(
			"200 OK", ct, payload, false);
		WARCRecordInfo recinfo = new TestWARCRecordInfo(recordBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		Resource payloadResource = new WarcResource(rec, ar);
		payloadResource.parseHeaders();

		final String range = "bytes=10-19";
		// range request
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)range$")))
			.andStubReturn(range);

		TestServletOutputStream servletOutput = new TestServletOutputStream();
		// expectations
		response.setStatus(206);
		EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
		Capture<String> contentRange = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-range$"),
			EasyMock.capture(contentRange));
		EasyMock.expectLastCall().once();
		Capture<String> contentLength = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-length$"),
			EasyMock.capture(contentLength));
		EasyMock.expectLastCall().once();
		response.setHeader(EasyMock.<String>anyObject(),
			EasyMock.<String>anyObject());
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(request, response, uriConverter);

		cut.renderResource(request, response, wbRequest, result,
			payloadResource, uriConverter, results);

		EasyMock.verify(response);

		assertTrue(contentRange.hasCaptured());
		String contentRangeValue = contentRange.getValue();
		assertEquals("bytes 10-19/1024", contentRangeValue);

		assertTrue(contentLength.hasCaptured());
		assertEquals("10", contentLength.getValue());

		byte[] outputBytes = servletOutput.getBytes();
		assertEquals(10, outputBytes.length);
		assertEquals(10, outputBytes[0]);
		assertEquals(19, outputBytes[9]);
    }

    public void testRenderResource_Range_From200_All() throws Exception {
		final String ct = "application/octet-stream";
		final byte[] payload = buildTestBytes(1024);
		final byte[] recordBytes = TestWARCRecordInfo.buildHttpResponseBlock(
			"200 OK", ct, payload, false);
		WARCRecordInfo recinfo = new TestWARCRecordInfo(recordBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		Resource payloadResource = new WarcResource(rec, ar);
		payloadResource.parseHeaders();

		final String range = "bytes=0-";
		// range request
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)range$")))
			.andStubReturn(range);

		TestServletOutputStream servletOutput = new TestServletOutputStream();
		// expectations
		response.setStatus(206);
		EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
		Capture<String> contentRange = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-range$"),
			EasyMock.capture(contentRange));
		EasyMock.expectLastCall().once();
		Capture<String> contentLength = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-length$"),
			EasyMock.capture(contentLength));
		EasyMock.expectLastCall().once();
		response.setHeader(EasyMock.<String>anyObject(),
			EasyMock.<String>anyObject());
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(request, response, uriConverter);

		cut.renderResource(request, response, wbRequest, result,
			payloadResource, uriConverter, results);

		EasyMock.verify(response);

		assertTrue(contentRange.hasCaptured());
		String contentRangeValue = contentRange.getValue();
		assertEquals("bytes 0-1023/1024", contentRangeValue);

		assertTrue(contentLength.hasCaptured());
		assertEquals("1024", contentLength.getValue());

		byte[] outputBytes = servletOutput.getBytes();
		assertEquals(1024, outputBytes.length);
		assertEquals(0, outputBytes[0]);
		assertEquals(-1, outputBytes[1023]);
    }

    public void testRenderResource_Range_From200_Tail() throws Exception {
		final String ct = "application/octet-stream";
		final byte[] payload = buildTestBytes(1024);
		final byte[] recordBytes = TestWARCRecordInfo.buildHttpResponseBlock(
			"200 OK", ct, payload, false);
		WARCRecordInfo recinfo = new TestWARCRecordInfo(recordBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		Resource payloadResource = new WarcResource(rec, ar);
		payloadResource.parseHeaders();

		final String range = "bytes=-10";
		// range request
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)range$")))
			.andStubReturn(range);

		TestServletOutputStream servletOutput = new TestServletOutputStream();
		// expectations
		response.setStatus(206);
		EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
		Capture<String> contentRange = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-range$"),
			EasyMock.capture(contentRange));
		EasyMock.expectLastCall().once();
		Capture<String> contentLength = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-length$"),
			EasyMock.capture(contentLength));
		EasyMock.expectLastCall().once();
		response.setHeader(EasyMock.<String>anyObject(),
			EasyMock.<String>anyObject());
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(request, response, uriConverter);

		cut.renderResource(request, response, wbRequest, result,
			payloadResource, uriConverter, results);

		EasyMock.verify(response);

		assertTrue(contentRange.hasCaptured());
		String contentRangeValue = contentRange.getValue();
		assertEquals("bytes 1014-1023/1024", contentRangeValue);

		assertTrue(contentLength.hasCaptured());
		assertEquals("10", contentLength.getValue());

		byte[] outputBytes = servletOutput.getBytes();
		assertEquals(10, outputBytes.length);
		assertEquals(-10, outputBytes[0]);
		assertEquals(-1, outputBytes[9]);
    }

	protected static byte[] buildPartialContentResponseBlock(String ctype,
			byte[] payloadBytes, long startPos, long instanceSize)
			throws IOException {
		assertTrue(startPos + payloadBytes.length <= instanceSize);
		final String CRLF = "\r\n";
		final String contentRange = String.format("bytes %d-%d/%d", startPos, startPos + payloadBytes.length - 1, instanceSize);
		ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
		Writer bw = new OutputStreamWriter(blockbuf);
		bw.write("HTTP/1.0 206 Partial Content" + CRLF);
		bw.write("Content-Type: " + ctype + CRLF);
		bw.write("Content-Range: " + contentRange + CRLF);
		bw.write("Content-Length: " + payloadBytes.length + CRLF);
		bw.write(CRLF);
		bw.close();
		blockbuf.write(payloadBytes);
		return blockbuf.toByteArray();
    }

    public void testRenderResource_Range_From206() throws Exception {
		final String ct = "application/octet-stream";
		final byte[] payload = buildTestBytes(1024);
		final byte[] recordBytes = buildPartialContentResponseBlock(ct,
			payload, 10, 1040);
		WARCRecordInfo recinfo = new TestWARCRecordInfo(recordBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		Resource payloadResource = new WarcResource(rec, ar);
		payloadResource.parseHeaders();

		final String range = "bytes=12-1032";
		// range request
		EasyMock.expect(request.getHeader(EasyMock.matches("(?i)range$")))
			.andStubReturn(range);

		TestServletOutputStream servletOutput = new TestServletOutputStream();
		// expectations
		response.setStatus(206);
		EasyMock.expect(response.getOutputStream()).andReturn(servletOutput);
		Capture<String> contentRange = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-range$"),
			EasyMock.capture(contentRange));
		EasyMock.expectLastCall().once();
		Capture<String> contentLength = new Capture<String>(CaptureType.FIRST);
		response.setHeader(EasyMock.matches("(?i)content-length$"),
			EasyMock.capture(contentLength));
		EasyMock.expectLastCall().once();
		response.setHeader(EasyMock.<String>anyObject(),
			EasyMock.<String>anyObject());
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(request, response, uriConverter);

		cut.renderResource(request, response, wbRequest, result,
			payloadResource, uriConverter, results);

		EasyMock.verify(response);

		assertTrue(contentRange.hasCaptured());
		String contentRangeValue = contentRange.getValue();
		assertEquals("bytes 12-1032/1040", contentRangeValue);

		assertTrue(contentLength.hasCaptured());
		assertEquals("1021", contentLength.getValue());

		byte[] outputBytes = servletOutput.getBytes();
		assertEquals(1021, outputBytes.length);
		assertEquals(2, outputBytes[0]);
		assertEquals(-2, outputBytes[1020]);
    }
}
