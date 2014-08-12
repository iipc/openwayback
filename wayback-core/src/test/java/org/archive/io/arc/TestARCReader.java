/**
 * 
 */
package org.archive.io.arc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.util.DateUtils;

import com.google.common.io.CountingInputStream;
import java.util.Locale;

/**
 * Fixture ARCReader.
 * <p>It works as ArchiveReader reading from ARC file with just one
 * WARC record at offset 0 (no version_block line).</p>
 * <p>record content is customized through {@link WARCRecordInfo}.
 * ({@link TestWARCRecordInfo} offers commonly-used default values and convenient factory
 * methods.</p>
 * 
 * TODO: could separate ARC record formatting code out of ARCWriter and reuse it here.
 * current ARCWriter requires too much boilerplate, always writes out the first metadata
 * line.
 * 
 * @see TestWARCReader
 * @contributor kenji
 *
 */
public class TestARCReader extends ARCReader {
    
    public TestARCReader(InputStream is) {
        setIn(is);
    }
    public TestARCReader(WARCRecordInfo recinfo) throws IOException {
        setIn(new CountingInputStream(buildRecordContent(recinfo)));
        // ARCRecord tries to read off version-block if offset==0 and
        // alignedOnFirstRecord is true (it is by default). As we don't have
        // version-block at offset 0, we disable this behavior.
        setAlignedOnFirstRecord(false);
    }
    
    @Override
    public ARCRecord get(long offset) throws IOException {
        return (ARCRecord)super.get(offset);
    }
    
    private String isozToDateTime14(String isoz) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH).parse(isoz);
            return DateUtils.get14DigitDate(d);
        } catch (ParseException ex) {
            throw new RuntimeException("bad ISOZ: " + isoz, ex);
        }
    }
    /**
     * build minimally-conforming ARC record byte stream. 
     * @param recinfo WARCRecordInfo with record metadata and content.
     *      type parameter does not matter, be sure to set contentType to that of
     *      payload.
     * @return InputStream reading from created record bits
     * @throws IOException
     */
    public InputStream buildRecordContent(WARCRecordInfo recinfo) throws IOException {
        final char FS = ARCWriter.HEADER_FIELD_SEPARATOR;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        String timeStamp = isozToDateTime14(recinfo.getCreate14DigitDate());
        String mimetype = recinfo.getMimetype();
        long contentLen = recinfo.getContentLength();
        // URL Record v1.
        String urlRecordLine = recinfo.getUrl() + FS + "4.4.4.4" + FS +
                timeStamp + FS + mimetype + FS + contentLen + ARCWriter.LINE_SEPARATOR;
        buf.write(urlRecordLine.getBytes("UTF-8"));
        IOUtils.copy(recinfo.getContentStream(), buf);
        buf.write(ARCWriter.LINE_SEPARATOR);
        
        return new ByteArrayInputStream(buf.toByteArray());
    }
}
