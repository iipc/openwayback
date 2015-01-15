/**
 * 
 */
package org.archive.wayback.resourcestore.resourcefile;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.GzipDecodingResource;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.charset.CharsetDetector;
import org.archive.wayback.replay.charset.StandardCharsetDetector;
import org.archive.wayback.resourcestore.jwat.JWATResourceTest;


/**
 * TODO: add more tests. it has only tests relevant to recent
 * changes.
 * 
 * @contributor kenji
 *
 */
public class WarcResourceTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * create a test {@link Resource} from {@link WARCRecordInfo} {@code recinfo}.
     * <p>Override this method to run tests on different implementations of
     * Resource.</p>
     * @param recinfo
     * @return Resource
     * @see JWATResourceTest
     */
    protected Resource createResource(WARCRecordInfo recinfo) throws Exception {
    	TestWARCReader ar = new TestWARCReader(recinfo);
    	WARCRecord rec = ar.get(0);
    	WarcResource res = new WarcResource(rec, ar);
    	return res;
    }

    /**
     * plain HTTP response (without any transfer/content-encoding)
     * @throws Exception
     */
    public void testPlainHttpRecord() throws Exception {
        String payload = "hogehogehogehogehoge";
        WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(payload);
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        assertEquals("statusCode", 200, res.getStatusCode());
        assertEquals("content-type", "text/plain", res.getHeader("Content-Type"));
        byte[] buf = new byte[payload.getBytes().length + 1];
        int n = res.read(buf);
        assertEquals("content length", buf.length - 1, n);
        
        res.close();
    }
    /**
     * uncompressed, but chunked-encoded HTTP response 
     * @throws Exception
     */
    public void testPlainChunkedHttpRecord() throws Exception {
        String payload = "hogehogehogehogehoge";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(
                TestWARCRecordInfo.buildHttpResponseBlock("200 OK",
                        "text/plain", payload.getBytes("UTF-8"), true));
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        assertEquals("statusCode", 200, res.getStatusCode());
        assertEquals("content-type", "text/plain", res.getHeader("Content-Type"));
        byte[] buf = new byte[payload.getBytes().length + 1];
        int n = res.read(buf);
        assertEquals("content length", buf.length - 1, n);
        
        res.close();
    }

    /**
     * gzip-compressed HTTP response.
     * @throws Exception
     */
    public void testCompressedHttpRecord() throws Exception {
        String payload = "hogehogehogehogehoge";
        String ctype = "text/plain";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(
                TestWARCRecordInfo.buildCompressedHttpResponseBlock(ctype,
                        payload.getBytes()));
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        assertEquals("statusCode", 200, res.getStatusCode());
        assertEquals("content-type", ctype, res.getHeader("Content-Type"));
        
        Resource zres = TextReplayRenderer.decodeResource(res);
        assertTrue("wrapped with GzipDecodingResource", (zres instanceof GzipDecodingResource));
        
        byte[] buf = new byte[payload.getBytes().length + 1];
        int n = zres.read(buf);
        assertEquals("content length", buf.length - 1, n);
        
        res.close();
    }
    
    /**
     * gzip-compressed, chunked-encoded HTTP response.
     * @throws Exception
     */
    public void testCompressedChunkedHttpRecord() throws Exception {
        String payload = "hogehogehogehogehoge";
        String ctype = "text/plain";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(
                TestWARCRecordInfo.buildCompressedHttpResponseBlock(ctype,
                        payload.getBytes(), true));
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        assertEquals("statusCode", 200, res.getStatusCode());
        assertEquals("content-type", ctype, res.getHeader("Content-Type"));
        
        Resource zres = TextReplayRenderer.decodeResource(res);
        assertTrue("wrapped with GzipDecodingResource", (zres instanceof GzipDecodingResource));
        
        byte[] buf = new byte[payload.getBytes().length + 1];
        int n = zres.read(buf);
        assertEquals("content length", buf.length - 1, n);
        
        res.close();
    }

    // TODO: add more tests on various Transfer-Encoding and Content-Encoding.
    // TODO: add more tests on corner cases.
    
    /**
     * metadata record with render-able content like site screenshot image.
     * 
     * HTTP status is assumed to be 200, and Content-Type of WARC header
     * becomes Content-Type of replay response.
     * @throws Exception
     */
    public void testMetadataRecord() throws Exception {
        // 1-dot transparent GIF found everywhere if you wonder :-)
        final byte[] block = new byte[] {
                71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, 0, 0, -64, -64, -64,
                0, 0, 0, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0,
                1, 0, 1, 0, 0, 2, 2, 68, 1, 0, 59, 13, 10, 13, 10
        };
        final String ct = "image/gif";
        WARCRecordInfo recinfo = new TestWARCRecordInfo(block);
        recinfo.setType(WARCRecordType.metadata);
        recinfo.setMimetype(ct);
        Resource res = createResource(recinfo);
        // must not fail
        res.parseHeaders();
        
        // should return assumed 200
        assertEquals("statusCode", 200, res.getStatusCode());
        // content-type is what's specified in WARC header.
        assertEquals("content-type", ct, res.getHeader("Content-Type"));
        // must have Date header, in HTTP Date format.
        String date = res.getHeader("Date");
        assertNotNull("has date header", date);
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(date);
        
        // block as content
        byte[] buf = new byte[block.length + 1];
        int n = res.read(buf);
        assertEquals("content length", block.length, n);
        for (int i = 0; i < block.length; i++) {
            assertEquals("byte " + i, block[i], buf[i]);
        }
        
        res.close();
    }
    
    final String REVISIT_WARC_PROFILE =
            "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest";
    /**
     * new, current revisit record, which has just HTTP response line and
     * headers part of the capture.
     * <p>Expectations:
     * TextReplayRender receives revisit WarcResource as {@code httpHeaderResource},
     * and calls following methods on it:</p>
     * <ul>
     * <li>{@link WarcResource#getStatusCode()}</li>
     * <li>{@link WarcResource#getHttpHeaders()} (ok to return null)</li>
     * </ul>
     * @throws Exception
     */
    public void testRevisitRecord() throws Exception {
        final String ct = "text/html";
        WARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse(ct, 1345);
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        // these are from this record.
        assertEquals("statusCode", 200, res.getStatusCode());
        assertEquals("content-type", ct, res.getHeader("Content-Type"));
        
        StandardCharsetDetector csd = new StandardCharsetDetector();
        // assuming WaybackRequest (3rd parameter) is not used in getCharset()
        csd.getCharset(res, res, null);

        res.close();
    }
    
    /**
     * old revisit record, which has zero-length block (no HTTP response
     * line, no HTTP headers).
     * 
     * in this case, {@link WarcResource#getStatusCode()} should not fail, but
     * either return special value or throw an appropriate exception signifying
     * there's no HTTP status line recorded in this resource, and thus ReplayRenderer
     * should fallback on using payloadResource for the info instead.
     * {@link WarcResource#getHttpHeaders()} must not return null, but should
     * return empty Map object, so that {@link CharsetDetector} can return null
     * without failing.
     * 
     * for the better, this fallback may be encapsulated in
     * virtual Resource combining httpHeaderResource and payloadResource.
     * 
     * related issue: https://webarchive.jira.com/browse/ACC-126
     * @throws Exception
     * @see TextReplayRenderer
     * @see StandardCharsetDetector#getCharset(org.archive.wayback.core.Resource, org.archive.wayback.core.Resource, org.archive.wayback.core.WaybackRequest)
     */
    public void testOldRevisitRecord() throws Exception {
        final String ct = "text/html";
        WARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse(ct, 1345, false);
        Resource res = createResource(recinfo);
        res.parseHeaders();
        
        // should either return special value or throw appropriate exception (TBD)
        int scode = res.getStatusCode();
        assertEquals("status code", 0, scode);
        
        Map<String, String> headers = res.getHttpHeaders();
        //assertNotNull("headers", headers);
        assertNull("headers", headers);
        
        res.close();
    }

    public void testUrlAgnosticRevisitRecord() throws Exception {
		final String ctype = "text/html";
		WARCRecordInfo recinfo = TestWARCRecordInfo
			.createUrlAgnosticRevisitHttpResponse(ctype, 1345);
        Resource res = createResource(recinfo);
        res.parseHeaders();

		// these are from this record.
		assertEquals("statusCode", 200, res.getStatusCode());
		assertEquals("content-type", ctype, res.getHeader("Content-Type"));

        assertEquals("http://example.com/", res.getRefersToTargetURI());
        assertEquals("20140101101010", res.getRefersToDate());

        StandardCharsetDetector csd = new StandardCharsetDetector();
        // assuming WaybackRequest (3rd parameter) is not used in getCharset()
        csd.getCharset(res, res, null);

        res.close();
    }

    /**
     * resource record, typically used for archiving ftp fetches.
     * @throws Exception
     */
    public void testResourceRecord() throws Exception {
        final String ct = "text/plain";
        final byte[] block = "blahblahblah\n".getBytes();
        WARCRecordInfo recinfo = new TestWARCRecordInfo(block);
        recinfo.setType(WARCRecordType.resource);
        recinfo.setUrl("ftp://ftp.example.com/afile.txt");
        recinfo.setMimetype(ct);
        Resource res = createResource(recinfo);
        res.parseHeaders();
                
        int scode = res.getStatusCode();
        assertEquals("statusCode", 200, scode);

        Map<String, String> headers = res.getHttpHeaders();
        assertNotNull("headers", headers);
        
        assertEquals("content-type", ct, res.getHeader("Content-Type"));

        // must have Date header, in HTTP Date format.
        String date = res.getHeader("Date");
        assertNotNull("has date header", date);
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(date);
        
        res.close();
    }
    
    // TODO: there can be revisit for ftp fetches, right?
}
