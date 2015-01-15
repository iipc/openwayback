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
		return createResource("text/html", payload, encoding);
	}
	protected WarcResource createResource(String contentType, String payload, String encoding) throws IOException {
		final byte[] payloadBytes = payload.getBytes(encoding);
		TestWARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse(contentType, payloadBytes);
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
	 * test of {@code x-} charset names.
	 * @throws Exception
	 */
	public void testXCharsetName() throws Exception {
		final String payload = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head>" +
				"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=x-sjis\" />" +
				"  <title>Test Document</title>" +
				"  <link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" />" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		WarcResource resource = createResource(payload, "x-sjis");
		WaybackRequest wbRequest = new WaybackRequest();

		RotatingCharsetDetector cut = new RotatingCharsetDetector();

		String charset = cut.getCharset(resource, wbRequest);

		assertEquals("x-sjis", charset);
	}

	/**
	 * test of {@code x-user-defined} charset name.
	 * mapped to {@code windows-1252}.
	 * @throws Exception
	 */
	public void testXUserDefined() throws Exception {
		final String payload = "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"<head>" +
				"  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=x-user-defined\" />" +
				"  <title>Test Document</title>" +
				"  <link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\" />" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		WarcResource resource = createResource(payload, "windows-1252");
		WaybackRequest wbRequest = new WaybackRequest();

		RotatingCharsetDetector cut = new RotatingCharsetDetector();

		String charset = cut.getCharset(resource, wbRequest);

		assertEquals("windows-1252", charset);
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
	
	/**
	 * test of {@link ContentTypeHeaderSniffer}
	 * @throws Exception
	 */
	public void testContentTypeHeaderSniffer() throws Exception {
		ContentTypeHeaderSniffer cut = new ContentTypeHeaderSniffer();
		final String payload = "<html>" +
				"<body>" +
				"</body>" +
				"</html>";
		{
			WarcResource resource = createResource("text/html", payload, "UTF-8");
			String enc = cut.sniff(resource);
			assertNull(enc);
		}
		{
			WarcResource resource = createResource("text/html;charset=utf-8", payload, "UTF-8");
			String enc = cut.sniff(resource);
			assertEquals("utf-8", enc);
		}
		{
			WarcResource resource = createResource("text/html; charset=shift_jis", payload, "shift_jis");
			String enc = cut.sniff(resource);
			assertEquals("shift_jis", enc);
		}
		{
			// rescuing broken charset name
			WarcResource resource = createResource("text/html; charset=i so-8859-1", payload, "iso-8859-1");
			String enc = cut.sniff(resource);
			// sniffer maps "iso-8859-1" to "cp1252";
			assertEquals("windows-1252", enc);
		}
	}
	// more tests?
}
