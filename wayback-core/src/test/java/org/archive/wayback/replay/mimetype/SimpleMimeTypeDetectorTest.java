package org.archive.wayback.replay.mimetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

/**
 * Test for {@link SimpleMimeTypeDetector}
 */
public class SimpleMimeTypeDetectorTest extends TestCase {

	SimpleMimeTypeDetector cut;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cut = new SimpleMimeTypeDetector();
	}

	public static Resource createTestResource(String ctype, byte[] payloadBytes)
			throws IOException {
		WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(ctype,
			payloadBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		WarcResource resource = new WarcResource(rec, ar);
		resource.parseHeaders();
		return resource;
	}

	protected byte[] getTestContent(String filename) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		InputStream is = getClass().getResourceAsStream(filename);
		assertNotNull("test resource " + filename + " is missing", is);
		byte[] buf = new byte[8192];
		int n;
		while ((n = is.read(buf)) > 0) {
			bao.write(buf, 0, n);
		}
		return bao.toByteArray();
	}

	protected String detectMimeType(String filename, String indexContentType,
			String recordContentType) throws IOException {
		//WaybackRequest wbRequest = new WaybackRequest();
		//CaptureSearchResult result = new CaptureSearchResult();
		//result.setMimeType(indexContentType);
		Resource resource = createTestResource(recordContentType,
			getTestContent(filename));
		String mimetype = cut.sniff(resource);
		return mimetype;
	}

	protected String detectMimeType(String filename, String indexContentType) throws IOException {
		String recordContentType = "unk".equals(indexContentType) ? null
				: indexContentType;
		return detectMimeType(filename, indexContentType, recordContentType);
	}

	// Content-Type sniffing tests

	public void testContentSniffing_HTML() throws Exception {
		// CDX writer writes "unk" in content-type field if Content-Type
		// header is missing.
		assertEquals("text/html", detectMimeType("html/1.html", "unk"));
		assertEquals("text/html", detectMimeType("html/2.html", "unk"));
		assertEquals("text/html", detectMimeType("html/3.html", "unk"));
	}

	public void testContentSniffing_JavaScript() throws Exception {
		assertEquals("text/javascript", detectMimeType("js/1.js", "text/html"));
	}

	public void testContentSniffing_JSON() throws Exception {
		assertEquals("application/json", detectMimeType("json/1.json", "application/javascript"));
	}

	public void testContentSniffing_CSS() throws Exception {
		assertEquals("text/css", detectMimeType("css/1.css", "unk"));
		assertEquals("text/css", detectMimeType("css/2.css", "unk"));
		assertEquals("text/css", detectMimeType("css/3.css", "unk"));
		assertEquals("text/css", detectMimeType("css/4.css", "unk"));
	}

	public void testContentSniffing_Binary() throws Exception {
		assertEquals("application/pdf", detectMimeType("bin/1.pdf", "unk"));
		assertEquals("image/png", detectMimeType("bin/2.png", "text/html"));
	}
}
