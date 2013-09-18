package org.archive.io.warc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.util.anvl.ANVLRecord;
import org.archive.util.anvl.Element;

import com.google.common.io.CountingInputStream;

/**
 * Fixture WARCReader.
 * <p>It works as ArchiveReader reading from WARC file with just one
 * WARC record at offset 0 (there's no "warcinfo" record).</p>
 * <p>Content of the record is customized through {@link WARCRecordInfo}.
 * ({@link TestWARCRecordInfo} offers commonly-used default values and convenient factory
 * methods.</p>
 * <p>Typical test code would be:</p>
 * <pre>
 * String payload = "hogehogehogehogehoge";
 * WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(payload);
 * TestWARCReader ar = new TestWARCReader(recinfo);
 * WARCRecord rec = (WARCRecord)ar.get(0);
 * </pre>
 * 
 * @contributor kenji
 *
 */
public class TestWARCReader extends ArchiveReader {
    public static final String CRLF = "\r\n";
    
    public TestWARCReader(InputStream is) {
        setIn(is);
    }
    public TestWARCReader(WARCRecordInfo recinfo) throws IOException {
        // not clearly stated, but ArchiveReader expects CountingInputStream.
        setIn(new CountingInputStream(TestWARCReader.buildRecordContent(recinfo)));
    }
    
    @Override
    public WARCRecord get(long offset) throws IOException {
        return (WARCRecord)super.get(offset);
    }
    @Override
    protected WARCRecord createArchiveRecord(InputStream is, long offset)
            throws IOException {
        return (WARCRecord)currentRecord(new WARCRecord(is, "<identifier>", offset));
    }
    @Override
    protected void gotoEOR(ArchiveRecord record) throws IOException {
    }
    @Override
    public String getFileExtension() {
        return "warc";
    }
    @Override
    public String getDotFileExtension() {
        return ".warc";
    }
    @Override
    public void dump(boolean compress) throws IOException, ParseException {
        // TODO Auto-generated method stub
    }
    @Override
    public ArchiveReader getDeleteFileOnCloseReader(File f) {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * build minimal WARC record byte stream.
     * @param recinfo WARCRecordInfo with record metadata and content
     * @return InputStream reading from created record bits
     * @throws IOException
     */
    public static InputStream buildRecordContent(WARCRecordInfo recinfo) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(buf);
        w.write("WARC/1.0" + CRLF);
        w.write("WARC-Type: " + recinfo.getType() + CRLF);
        if (StringUtils.isNotEmpty(recinfo.getUrl())) {
            w.write("WARC-Target-URI: " + recinfo.getUrl() + CRLF);
        }
        w.write("WARC-Date: " + recinfo.getCreate14DigitDate() + CRLF);
        if (recinfo.getExtraHeaders() != null) {
            ANVLRecord headers = recinfo.getExtraHeaders();
            for (Element el : headers) {
                w.write(el.getLabel() + ": " + el.getValue() + CRLF);
            }
        }
        w.write("Content-Type: " + recinfo.getMimetype() + CRLF);
        w.write("Content-Length: " + recinfo.getContentLength() + CRLF);
        w.write(CRLF);
        w.flush();
        IOUtils.copy(recinfo.getContentStream(), buf);
        buf.write((CRLF+CRLF).getBytes());
        buf.close();
        
        return new ByteArrayInputStream(buf.toByteArray());
    }        
}
