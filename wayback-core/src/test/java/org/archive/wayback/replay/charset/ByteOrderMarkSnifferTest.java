package org.archive.wayback.replay.charset;

import java.io.IOException;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

public class ByteOrderMarkSnifferTest extends TestCase {

	ByteOrderMarkSniffer cut;
	Resource resource;

	protected void setUp() throws Exception {
		cut = new ByteOrderMarkSniffer();
	}

	protected void setupResource(byte[] payload) throws IOException {
		TestWARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse("text/html", payload);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		resource = new WarcResource(rec, ar);
		resource.parseHeaders();
	}

	public void testUTF16BE() throws Exception {
		setupResource(new byte[] { (byte)0xFE, (byte)0xFF, '<', 'H', 'T', 'M', 'L', '>', '<', '/', 'H', 'T', 'M', 'L', '>' });
		String detected = cut.sniff(resource);
		assertEquals("UTF-16BE", detected);
	}

	public void testUTF16LE() throws Exception {
		setupResource(new byte[] { (byte)0xFF, (byte)0xFE, '<', 'H', 'T', 'M', 'L', '>', '<', '/', 'H', 'T', 'M', 'L', '>' });
		String detected = cut.sniff(resource);
		assertEquals("UTF-16LE", detected);
	}

	public void testUTF8() throws Exception {
		setupResource(new byte[] { (byte)0xEF, (byte)0xBB, (byte)0xBF, '<', 'H', 'T', 'M', 'L', '>', '<', '/', 'H', 'T', 'M', 'L', '>' });
		String detected = cut.sniff(resource);
		assertEquals("UTF-8", detected);
	}

	public void testNoBOM() throws Exception {
		setupResource(new byte[] { '<', 'H', 'T', 'M', 'L', '>', '<', '/', 'H', 'T', 'M', 'L', '>' });
		String detected = cut.sniff(resource);
		assertNull(detected);
	}
}
