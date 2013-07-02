/**
 * 
 */
package org.archive.wayback.resourcestore.resourcefile;

import java.text.SimpleDateFormat;
import java.util.Map;

import junit.framework.TestCase;

import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.TestARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.GzipDecodingResource;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.charset.StandardCharsetDetector;

/**
 * @author kenji
 *
 */
public class ArcResourceTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testPlainHttpRecord() throws Exception {
        String payload = "hogehogehogehogehoge";
        WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(payload);
        recinfo.setMimetype("text/plain");
        TestARCReader ar = new TestARCReader(recinfo);
        ARCRecord rec = ar.get(0);
        ArcResource res = new ArcResource(rec, ar);
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
        recinfo.setMimetype(ctype);
        TestARCReader ar = new TestARCReader(recinfo);
        ARCRecord rec = ar.get(0);
        ArcResource res = new ArcResource(rec, ar);
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
    
    // NOTE: ARC revisit records have zero-length content, and ArcResource never gets created for them.
    // thus we don't need a test case for revisit ARCRecord.
//    /**
//     * new, current revisit record, which has just HTTP response line and
//     * headers part of the capture.
//     * <p>Expectations:
//     * TextReplayRender receives revisit WarcResource as {@code httpHeaderResource},
//     * and calls following methods on it:</p>
//     * <ul>
//     * <li>{@link WarcResource#getStatusCode()}</li>
//     * <li>{@link WarcResource#getHttpHeaders()} (ok to return null)</li>
//     * </ul>
//     * @throws Exception
//     */
//    public void testRevisitRecord() throws Exception {
//        final String ct = "text/html";
//        WARCRecordInfo recinfo = TestWARCRecordInfo.createRevisitHttpResponse(ct, 1345, false);
//        recinfo.setMimetype(ct);
//        TestARCReader ar = new TestARCReader(recinfo);
//        ARCRecord rec = ar.get(0);
//        ArcResource res = new ArcResource(rec, ar);
//        res.parseHeaders();
//        
//        // these are from this record.
//        assertEquals("statusCode", 200, res.getStatusCode());
//        assertEquals("content-type", ct, res.getHeader("Content-Type"));
//        
//        StandardCharsetDetector csd = new StandardCharsetDetector();
//        // assuming WaybackRequest (3rd parameter) is not used in getCharset()
//        csd.getCharset(res, res, null);
//
//        res.close();
//    }
    
    // disabled because it seems to be a general assumption that there exists no
    // ARC records for FTP (or non-HTTP).
//    /**
//     * ARC record for ftp fetches.
//     * @throws Exception
//     */
//    public void testResourceRecord() throws Exception {
//        final String ct = "text/plain";
//        final byte[] block = "blahblahblah\n".getBytes();
//        WARCRecordInfo recinfo = new TestWARCRecordInfo(block);
//        //recinfo.setType(WARCRecordType.resource);
//        recinfo.setUrl("ftp://ftp.example.com/afile.txt");
//        recinfo.setMimetype(ct);
//        TestARCReader ar = new TestARCReader(recinfo);
//        ARCRecord rec = (ARCRecord)ar.get(0);
//        ArcResource res = new ArcResource(rec, ar);
//        res.parseHeaders();
//                
//        int scode = res.getStatusCode();
//        assertEquals("statusCode", 200, scode);
//
//        Map<String, String> headers = res.getHttpHeaders();
//        assertNotNull("headers", headers);
//        
//        assertEquals("content-type", ct, res.getHeader("Content-Type"));
//
//        // must have Date header, in HTTP Date format.
//        String date = res.getHeader("Date");
//        assertNotNull("has date header", date);
//        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(date);
//        
//        res.close();
//    }
    
}
