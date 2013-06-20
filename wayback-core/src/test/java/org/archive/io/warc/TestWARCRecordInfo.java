package org.archive.io.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;

import org.archive.format.ArchiveFileConstants;
import org.archive.format.warc.WARCConstants;
import org.archive.util.DateUtils;

/**
 * WARCRecordInfo with default values and convenience factory methods.
 * @see TestWARCReader
 * @contributor kenji
 */
public class TestWARCRecordInfo extends WARCRecordInfo implements WARCConstants, ArchiveFileConstants {

    final static String REVISIT_WARC_PROFILE =
            "http://netpreserve.org/warc/1.0/revisit/identical-payload-digest";
    
    public TestWARCRecordInfo(byte[] content) {
        this.type = WARCRecordType.response;
        this.url = "http://test.example.com/";
        this.mimetype = "application/http; msgtype=response";
        try {
            this.recordId = new URI("uri:recordidentifier");
        } catch (URISyntaxException ex) {
            throw new RuntimeException("unexpected error", ex);
        }
        this.contentStream = new ByteArrayInputStream(content);
        this.contentLength = content.length;
        this.create14DigitDate = DateUtils.getLog14Date();
    }
    
    // factory methods
    public static TestWARCRecordInfo createHttpResponse(String payload) 
            throws IOException {
        return new TestWARCRecordInfo(buildHttpResponseBlock(payload));
    }
    public static TestWARCRecordInfo createHttpResponse(String ctype, byte[] payloadBytes)
            throws IOException {
        return new TestWARCRecordInfo(buildHttpResponseBlock(ctype, payloadBytes));
    }
    public static TestWARCRecordInfo createRevisitHttpResponse(String ctype, int len, boolean withHeader)
            throws IOException {
        TestWARCRecordInfo recinfo = new TestWARCRecordInfo(buildRevisitHttpResponseBlock(ctype, len, withHeader));
        recinfo.setType(WARCRecordType.revisit);
        recinfo.addExtraHeader("WARC-Truncated", "length");
        recinfo.addExtraHeader("WARC-Profile", REVISIT_WARC_PROFILE);
        return recinfo;
        
    }
    public static TestWARCRecordInfo createRevisitHttpResponse(String ctype, int len)
            throws IOException {
        return createRevisitHttpResponse(ctype, len, true);
    }
    
    
    public static byte[] buildHttpResponseBlock(String payload) throws IOException {
        return buildHttpResponseBlock("text/plain", payload.getBytes());
    }

    public static byte[] buildHttpResponseBlock(String ctype, byte[] payloadBytes)
            throws IOException {
        ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
        Writer bw = new OutputStreamWriter(blockbuf);
        bw.write("HTTP/1.0 200 OK" + CRLF);
        bw.write("Content-Length: " + payloadBytes.length + CRLF);
        bw.write("Content-Type: " + ctype + CRLF);
        bw.write(CRLF);
        bw.flush();
        blockbuf.write(payloadBytes);
        bw.close();
        return blockbuf.toByteArray();
    }
    
    /**
     * generates WARC content for new revisit record.
     * @param ctype value for Content-Type
     * @param len value for Content-Length
     * @param withHeader include HTTP status line and headers.
     *      passing false generates old-style revisit content block.
     * @return record content as byte array
     * @throws IOException
     */
    public static byte[] buildRevisitHttpResponseBlock(String ctype, int len,
            boolean withHeader) throws IOException {
        ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
        Writer bw = new OutputStreamWriter(blockbuf);
        if (withHeader) {
            bw.write("HTTP/1.0 200 OK" + CRLF);
            bw.write("Content-Length: " + len + CRLF);
            bw.write("Content-Type: " + ctype + CRLF);
            bw.write(CRLF);
            bw.flush();
            bw.close();
        }
        return blockbuf.toByteArray();
    }

}
