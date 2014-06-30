/**
 * 
 */
package org.archive.wayback.replay;

import java.io.IOException;
import java.util.Arrays;

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
import org.archive.wayback.replay.selector.AlwaysMatchSelector;
import org.archive.wayback.replay.selector.MimeTypeSelector;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

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
		final MimeTypeSelector CSS_SELECTOR = new MimeTypeSelector();
		CSS_SELECTOR.setMimeContains(Arrays.asList(new String[] {
			"text/javascript", "application/javascript",
			"application/x-javascript" }));
		CSS_SELECTOR.setRenderer(new TestReplayRenderer("js"));
		final AlwaysMatchSelector TRANS_SELECTOR = new AlwaysMatchSelector();
		TRANS_SELECTOR.setRenderer(new TestReplayRenderer("trans"));

		final ReplayRendererSelector[] SELECTORS = { CSS_SELECTOR,
			TRANS_SELECTOR, };
		cut = new SelectorReplayDispatcher();
		cut.setSelectors(Arrays.asList(SELECTORS));
	}

	public static Resource createTestTextResource(String ctype, byte[] payloadBytes)
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
		Resource resource = createTestTextResource("text/javascript", "var i=1;".getBytes("UTF-8"));
		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);		
	}

	public void testMimeTypeForced() throws Exception {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setJSContext(true);
		CaptureSearchResult result = new CaptureSearchResult();
		result.setMimeType("text/plain");
		Resource resource = createTestTextResource("text/plain", "a".getBytes("UTF-8"));
		ReplayRenderer rr = cut.getRenderer(wbRequest, result, resource);

		assertEquals("js", ((TestReplayRenderer)rr).name);
	}

}
