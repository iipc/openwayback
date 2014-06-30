/**
 * 
 */
package org.archive.wayback.archivalurl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.proxy.ProxyHttpsResultURIConverter;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.RedirectRewritingHttpHeaderProcessor;
import org.archive.wayback.replay.TransparentReplayRendererTest.TestServletOutputStream;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.IdentityResultURIConverterFactory;
import org.archive.wayback.replay.html.transformer.JSStringTransformer;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;
import org.easymock.EasyMock;

/**
 * Test case for {@link ArchivalURLJSStringTransformerReplayRenderer}.
 * 
 * @author kenji
 *
 */
public class ArchivalURLJSStringTransformerReplayRendererTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public static Resource createTestJSResource(byte[] payloadBytes) throws IOException {
		WARCRecordInfo recinfo = TestWARCRecordInfo.createHttpResponse("text/javascript", payloadBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		WarcResource resource = new WarcResource(rec, ar);
		resource.parseHeaders();
		return resource;
	}
	/**
	 * multi-component test with typical usage pattern:
	 * rewriting {@code https://} to {@code http://} in proxy-mode.
	 * (ArchivalURLJSStringTransformerReplayRenderer is also used in
	 * proxy-mode despite its name.)
	 * 
	 * @see org.archive.wayback.replay.html.transformer.JSStringTransformerTest
	 */
	public void testProxyHttpsTranslation() throws Exception {
		HttpHeaderProcessor httpHeaderProcessor = new RedirectRewritingHttpHeaderProcessor();
		ArchivalURLJSStringTransformerReplayRenderer renderer = new ArchivalURLJSStringTransformerReplayRenderer(
				httpHeaderProcessor);
		// not testing jspInserts - TODO
		
		// in production transformer is a MultiRegexReplaceStringTransformer
		// running other rewrites besides JSStringTransformer.  We are
		// only testing JSStringTransformer here.
		JSStringTransformer transformer = new JSStringTransformer();
		renderer.setTransformer(transformer);
		
		ResultURIConverter proxyURIConverter = new ProxyHttpsResultURIConverter();
		ContextResultURIConverterFactory converterFactory =
				new IdentityResultURIConverterFactory(proxyURIConverter);
		renderer.setConverterFactory(converterFactory);
		renderer.setRewriteHttpsOnly(true);
		
		final String payload =
				"var img1 = 'https://home.archive.org/~hstern/ARI-3745/happy_face.jpg';\n" +
				"var el1 = document.createElement('img');\n" +
				"el1.src = img;\n" +
				"document.getElementById('imgdiv').appendChild(el1)\n";
		final String expected =
				"var img1 = 'http://home.archive.org/~hstern/ARI-3745/happy_face.jpg';\n" +
				"var el1 = document.createElement('img');\n" +
				"el1.src = img;\n" +
				"document.getElementById('imgdiv').appendChild(el1)\n";
		
		final byte[] payloadBytes = payload.getBytes("UTF-8");
		Resource payloadResource = createTestJSResource(payloadBytes);
		
		// ResultURIConverter argument is passed down from AccessPoint#getUriConverter().
		// it is typically ProxyHttpsResultURIConverter(), the same class for converterFactory
		// (but a separate instance) - we reuse proxyURIConverter above.
		HttpServletResponse response = EasyMock.createNiceMock(HttpServletResponse.class);
		TestServletOutputStream servletOutput = new TestServletOutputStream();
		EasyMock.expect(response.getOutputStream()).andStubReturn(servletOutput);
		
		HttpServletRequest request = null; // assuming unused
		WaybackRequest wbRequest = new WaybackRequest();
		CaptureSearchResult result = new CaptureSearchResult();
		result.setOriginalUrl("http://home.archive.org/~hstern/ARI-3745/");
		
		EasyMock.replay(response);
		
		renderer.renderResource(request, response, wbRequest, result, payloadResource, payloadResource, proxyURIConverter, null);
		
		String out = servletOutput.getString();
		assertEquals("servlet output", expected, out);
	}

}
