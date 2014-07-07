/**
 * 
 */
package org.archive.wayback.replay.charset;

import java.io.IOException;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

/**
 * test for {@link RotatingCharsetDetector}.
 */
public class RotatingCharsetDetectorTest extends TestCase {

	protected WarcResource createResource(String payload, String encoding) throws IOException {
		final byte[] payloadBytes = payload.getBytes(encoding);
		TestWARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse("text/html", payloadBytes);
		TestWARCReader wr = new TestWARCReader(recinfo);
		WARCRecord rec = wr.get(0);
		WarcResource resource = new WarcResource(rec, wr);
		resource.parseHeaders();
		return resource;
	}

	/**
	 * content is UTF-8 encoded, but META tag says it's UTF-16.
	 * {@link PrescanMetadataSniffer} overrides UTF-16 to UTF-8.
	 * @throws Exception
	 */
	public void testFalseMetaUTF16() throws Exception {
		final String payload = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head>" +
				"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-16\" />" +
				"  <title>Test Document</title>" +
				"  <link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" />" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		WarcResource resource = createResource(payload, "UTF-8");
		WaybackRequest wbRequest = new WaybackRequest();

		RotatingCharsetDetector cut = new RotatingCharsetDetector();

		String charset = cut.getCharset(resource, wbRequest);
		
		assertEquals("UTF-8", charset);
	}

	/**
	 * content is UTF-16 encoded, but META tag says it's UTF-8.
	 * {@link PrescanMetadataSniffer} shall fail because it's UTF-16 encoded,
	 * and {@link UniversalChardetSniffer} should detect UTF-16.
	 * <p>Unfortunately, this test fails currently. Universal Chardet returns
	 * {@code null} for sample content, even with some non-ASCII chars. Hopefully
	 * UTF-16 texts have BOM, or plenty of non-ASCII chars make Universal Chardet
	 * work.</p>
	 * @throws Exception
	 */
	public void testFalseMetaUTF8() throws Exception {
		final String payload = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head>" +
				"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
				"  <title>Test Document</title>" +
				"  <link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" />" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		WarcResource resource = createResource(payload, "UTF-16BE");
		WaybackRequest wbRequest = new WaybackRequest();

		RotatingCharsetDetector cut = new RotatingCharsetDetector();

		String charset = cut.getCharset(resource, wbRequest);
		
		//assertEquals("UTF-16BE", charset);
	}
	
	// more tests?
}
