/**
 * 
 */
package org.archive.wayback.replay.html.transformer;

import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

/**
 * unit test for {@link BlockCSSStringTransformer}.
 * 
 */
public class SrcsetStringTransformerTest extends TestCase {

	URL baseURL;
	RecordingReplayParseContext rc;
	SrcsetStringTransformer st;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		baseURL = new URL("http://foo.com");
		rc = new RecordingReplayParseContext(null, baseURL, null);
		st = new SrcsetStringTransformer();
	}

	public void testTransform() throws Exception {
		final String[][] cases = new String[][] {
				{
					"http://example.com/files/pic-150.jpg 150w, http://example.com/files/pic.jpg 160w",
					"http://example.com/files/pic-150.jpg",
                                        "http://example.com/files/pic.jpg"
				},
				{
					"examples/img/lg.jpg, examples/img/xl.jpg 2x",
					"examples/img/lg.jpg",
                                        "examples/img/xl.jpg"
				}
		};
		
		for (String[] c : cases) {
			rc = new RecordingReplayParseContext(null, baseURL, null);
			st.transform(rc, c[0]);
			assertEquals(c[0], 2, rc.got.size());
			assertEquals(c[0], c[1], rc.got.get(0));
                        assertEquals(c[0], c[2], rc.got.get(1));
		}
	}
}
