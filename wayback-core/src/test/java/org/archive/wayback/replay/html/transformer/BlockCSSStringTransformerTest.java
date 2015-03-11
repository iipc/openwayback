/**
 * 
 */
package org.archive.wayback.replay.html.transformer;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

/**
 * unit test for {@link BlockCSSStringTransformer}.
 * 
 * @author kenji
 *
 */
public class BlockCSSStringTransformerTest extends TestCase {

	String baseURL;
	RecordingReplayParseContext rc;
	BlockCSSStringTransformer st;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		baseURL = "http://foo.com";
		rc = new RecordingReplayParseContext(baseURL, null);
		st = new BlockCSSStringTransformer();
	}

	public void testTransform() throws Exception {
        final String input = 
        		"@import \"style1.css\";\n" +
        		"@import 'style2.css';\n" +
        		"@import 'http://archive.org/common.css';\n" +
        		"BODY {\n" +
        		"  color: #fff;\n" +
        		"  background: transparent url(bg.gif);\n" +
        		"}\n";
        st.transform(rc, input);
        
        assertEquals(4, rc.got.size());
        assertTrue(rc.got.contains("style1.css"));
        assertTrue(rc.got.contains("style2.css"));
        assertTrue(rc.got.contains("http://archive.org/common.css"));
        assertTrue(rc.got.contains("bg.gif"));
	}
}
