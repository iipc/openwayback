/**
 * 
 */
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.mimetype.MimeTypeDetector;
import org.archive.wayback.replay.mimetype.SimpleMimeTypeDetector;
import org.archive.wayback.replay.selector.AlwaysMatchSelector;
import org.archive.wayback.replay.selector.MimeTypeSelector;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.easymock.EasyMock;

/**
 * test for {@link SelectorReplayDispatcher} and {@link ReplayRendererSelector}
 * implementations.
 * 
 */
public class SelectorReplayDispatcherTest extends TestCase {

	SelectorReplayDispatcher cut;

	static class TestReplayRenderer implements ReplayRenderer {
		String name;

		public TestReplayRenderer(String name) {
			this.name = name;
		}

		@Override
		public void renderResource(HttpServletRequest httpRequest,
				HttpServletResponse httpResponse, WaybackRequest wbRequest,
				CaptureSearchResult result, Resource resource,
				ResultURIConverter uriConverter, CaptureSearchResults results) {
		}

		@Override
		public void renderResource(HttpServletRequest httpRequest,
				HttpServletResponse httpResponse, WaybackRequest wbRequest,
				CaptureSearchResult result, Resource httpHeadersResource,
				Resource payloadResource, ResultURIConverter uriConverter,
				CaptureSearchResults results) {
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		final MimeTypeSelector HTML_SELECTOR = new MimeTypeSelector();
		HTML_SELECTOR.setMimeContains(Arrays.asList(new String[] {
			"text/html", "application/xhtml"
		}));
		HTML_SELECTOR.setRenderer(new TestReplayRenderer("html"));

		final MimeTypeSelector JS_SELECTOR = new MimeTypeSelector();
		JS_SELECTOR.setMimeContains(Arrays.asList(new String[] {
			"text/javascript", "application/javascript",
		"application/x-javascript" }));
		JS_SELECTOR.setRenderer(new TestReplayRenderer("js"));

		final MimeTypeSelector JSON_SELECTOR = new MimeTypeSelector();
		JSON_SELECTOR.setMimeContains(Arrays.asList(new String[] {
			"application/json"
		}));
		JSON_SELECTOR.setRenderer(new TestReplayRenderer("json"));

		final MimeTypeSelector CSS_SELECTOR = new MimeTypeSelector();
		CSS_SELECTOR.setMimeContains(Collections.singletonList("text/css"));
		CSS_SELECTOR.setRenderer(new TestReplayRenderer("css"));

		final AlwaysMatchSelector TRANS_SELECTOR = new AlwaysMatchSelector();
		TRANS_SELECTOR.setRenderer(new TestReplayRenderer("trans"));

		final ReplayRendererSelector[] SELECTORS = { HTML_SELECTOR,
			CSS_SELECTOR, JS_SELECTOR, JSON_SELECTOR, TRANS_SELECTOR, };
		cut = new SelectorReplayDispatcher();

		cut.setSelectors(Arrays.asList(SELECTORS));

		List<MimeTypeDetector> sniffers = new ArrayList<MimeTypeDetector>();
		sniffers.add(new SimpleMimeTypeDetector());
		cut.setMimeTypeDetectors(sniffers);
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

	public void testMimeTypeFromIndex() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("text/javascript");
		Resource resource = createTestResource("text/javascript", "var i=1;".getBytes("UTF-8"));
		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);
	}

	public void testMimeTypeForced() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setJSContext(true);
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("text/plain");
		Resource resource = createTestResource("text/plain", "a".getBytes("UTF-8"));
		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);
	}

	// Test interface with MimeTypeDetectors

	/**
	 * {@link MimeTypeDetector} overrides Content-Type in CaptureSearchResult.
	 * @throws Exception
	 */
	public void testMimeTypeDetector() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("unk");
		Resource resource = createTestResource(null,
			"<html></html>".getBytes("UTF-8"));

		MimeTypeDetector detector = EasyMock.createMock(MimeTypeDetector.class);
		EasyMock.expect(detector.sniff(resource)).andReturn("text/javascript");
		cut.setMimeTypeDetectors(Collections.singletonList(detector));

		EasyMock.replay(detector);

		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);
		EasyMock.verify(detector);
	}

	/**
	 * ContentTypeSniffer overrides Content-Type in CaptureSearchResult.
	 * @throws Exception
	 */
	public void testMimeTypeDetector_ignoredIfForced() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setCSSContext(true);
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("unk");
		Resource resource = createTestResource(null,
			"body { margin: 0 }".getBytes("UTF-8"));

		MimeTypeDetector detector = EasyMock.createMock(MimeTypeDetector.class);
		// no call to ontentTypeSniffer
		//EasyMock.expect(sniffer.sniff(resource)).andReturn("text/javascript");
		cut.setMimeTypeDetectors(Collections.singletonList(detector));

		EasyMock.replay(detector);

		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertNotNull(rr);
		assertEquals("css", ((TestReplayRenderer)rr).name);
		EasyMock.verify(detector);
	}

	/**
	 * Test of non-default {@code missingMimeType}.
	 * @throws Exception
	 */
	public void testMissingMimeType() throws Exception {
		final String MISSING_MIMETYPE = "application/http";

		WaybackRequest wbRequest = new WaybackRequest();
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType(MISSING_MIMETYPE);
		Resource resource = createTestResource(null,
			"var k = 1;".getBytes("UTF-8"));

		MimeTypeDetector detector = EasyMock.createMock(MimeTypeDetector.class);
		EasyMock.expect(detector.sniff(resource)).andReturn("text/javascript");
		cut.setMimeTypeDetectors(Collections.singletonList(detector));
		cut.setMissingMimeType(MISSING_MIMETYPE);

		EasyMock.replay(detector);

		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);
		EasyMock.verify(detector);

	}

	/**
	 * Use mime-type in CaptureSearchResult if MimeTypeDetector returns {@code null}.
	 * @throws Exception
	 */
	public void testMimeTypeDetector_useIndexMimeTypeIfDetectionFailed() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("text/html");
		Resource resource = createTestResource("text/html; charset=UTF-8", "<TR><TD>A</TD></TR>".getBytes("UTF-8"));

		MimeTypeDetector detector = EasyMock.createMock(MimeTypeDetector.class);
		EasyMock.expect(detector.sniff(resource)).andReturn(null);
		cut.setMimeTypeDetectors(Collections.singletonList(detector));

		EasyMock.replay(detector);

		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("html", ((TestReplayRenderer)rr).name);
		EasyMock.verify(detector);
	}

	// TODO: want another test for REVISIT case?

}
