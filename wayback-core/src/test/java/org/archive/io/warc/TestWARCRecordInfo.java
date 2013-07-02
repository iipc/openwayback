package org.archive.io.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.GZIPOutputStream;

import org.archive.format.ArchiveFileConstants;
import org.archive.format.warc.WARCConstants;
import org.archive.util.DateUtils;

/**
 * WARCRecordInfo with default values and convenience factory methods.
 * 
 * TODO: use existing well-tested HTTP library for generating HTTP content-block.
 * 
 * @see TestWARCReader
 * @contributor kenji
 * 
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
    
    /**
     * return TestWARCRecordInfo for HTTP Response with entity {@code payload}.
     * Content-Type is {@code text/plain}, and {@code payload} is encoded in UTF-8.
     * @param payload
     * @return
     * @throws IOException
     */
    public static TestWARCRecordInfo createHttpResponse(String payload) 
            throws IOException {
        return new TestWARCRecordInfo(buildHttpResponseBlock("text/plain", payload.getBytes("UTF-8")));
    }
    /**
     * return TestWARCRecordInfo for HTTP Response with entity {@code payload}.
     * @param ctype Content-Type value
     * @param payloadBytes payload bytes
     * @return WARCRecordInfo with default values set to key properties.
     * @throws IOException
     */
    public static TestWARCRecordInfo createHttpResponse(String ctype, byte[] payloadBytes)
            throws IOException {
        return new TestWARCRecordInfo(buildHttpResponseBlock(ctype, payloadBytes));
    }
    public static TestWARCRecordInfo createCompressedHttpResponse(String ctype,
            byte[] payloadBytes) throws IOException {
        return new TestWARCRecordInfo(buildCompressedHttpResponseBlock(ctype, payloadBytes));
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

    /**
     * short cut for generating "200 OK" HTTP response content-block.
     * @param ctype HTTP Content-Type, such as {@code "text/plain"}, {@code "image/gif"}
     * @param payloadBytes payload bytes
     * @return content-block bytes with HTTP status line, HTTP headers and payload.
     * @throws IOException
     */
    public static byte[] buildHttpResponseBlock(String ctype, byte[] payloadBytes) throws IOException {
        return buildHttpResponseBlock("200 OK", ctype, payloadBytes);
    }
    
    /**
     * return content-block bytes for HTTP response.
     * @param status HTTP status code and status text separated by a space. ex. {@code "200 OK"}.
     * @param ctype HTTP Content-Type
     * @param payloadBytes payload bytes
     * @return content-block bytes with HTTP status line, HTTP headers and payload.
     * @throws IOException
     */
    public static byte[] buildHttpResponseBlock(String status, String ctype, byte[] payloadBytes)
            throws IOException {
        ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
        Writer bw = new OutputStreamWriter(blockbuf);
        bw.write("HTTP/1.0 " + status + CRLF);
        bw.write("Content-Length: " + payloadBytes.length + CRLF);
        bw.write("Content-Type: " + ctype + CRLF);
        bw.write(CRLF);
        bw.flush();
        blockbuf.write(payloadBytes);
        bw.close();
        return blockbuf.toByteArray();
    }
    
    public static byte[] buildHttpRedirectResponseBlock(String location) throws IOException {
        ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
        Writer bw = new OutputStreamWriter(blockbuf);
        String status = "302 Moved Temporarily";
        bw.write("HTTP/1.0 " + status + CRLF);
        bw.write("Content-Length: " + 0 + CRLF);
        bw.write("Content-Type: text/html" + CRLF);
        bw.write("Location: " + location + CRLF);
        bw.write(CRLF);
        bw.close();
        return blockbuf.toByteArray();
    }
    
    public static byte[] buildCompressedHttpResponseBlock(String ctype,
            byte[] payloadBytes) throws IOException {
        ByteArrayOutputStream gzippedPayloadBytes = new ByteArrayOutputStream();
        GZIPOutputStream zout = new GZIPOutputStream(gzippedPayloadBytes);
        zout.write(payloadBytes);
        zout.close();
        payloadBytes = gzippedPayloadBytes.toByteArray();
        ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
        Writer bw = new OutputStreamWriter(blockbuf);
        bw.write("HTTP/1.0 200 OK" + CRLF);
        bw.write("Content-Length: " + payloadBytes.length + CRLF);
        bw.write("Content-Type: " + ctype + CRLF);
        bw.write("Content-Encoding: gzip" + CRLF);
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

    // POPULAR PAYLOAD SAMPLES
    
    // ubiquitous 1-pixel transparent GIF, if you wonder.
    public static final byte[] PAYLOAD_GIF = new byte[] {
            71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, 0, 0, -64, -64, -64,
            0, 0, 0, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0,
            1, 0, 1, 0, 0, 2, 2, 68, 1, 0, 59, 13, 10, 13, 10
    };

}
