/**
 * 
 */
package org.archive.wayback.replay.html.transformer;

import java.net.URL;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

import junit.framework.TestCase;

/**
 * unit test for {@link BlockCSSStringTransformer}.
 * 
 * @author kenji
 *
 */
public class BlockCSSStringTransformerTest extends TestCase {

	URL baseURL;
	RecordingReplayParseContext rc;
	BlockCSSStringTransformer st;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		baseURL = new URL("http://foo.com");
		rc = new RecordingReplayParseContext(null, baseURL, null);
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
	
	/**
	 * test of rewriteHttpsOnly.
	 * <p>Now it doesn't affect StringTransformer's behavior. This test
	 * will be dropped soon.</p>
	 * @throws Exception
	 */
	public void testRewriteHttpsOnly() throws Exception {
		rc.setRewriteHttpsOnly(true);
		
        final String input = 
        		"@import \"style1.css\";\n" +
        		"@import 'style2.css';\n" +
        		"@import 'http://archive.org/common.css';\n" +
        		"@import 'https://secure.archive.org/common.css';\n" +
        		"BODY {\n" +
        		"  color: #fff;\n" +
        		"  background: transparent url(bg.gif);\n" +
        		"}" +
        		".error {\n" +
        		"  background-image: url(https://secure.archive.org/img/error-icon.gif);\n" +
        		"}\n";
        st.transform(rc, input);
        
        assertEquals(6, rc.got.size());
        assertTrue(rc.got.contains("https://secure.archive.org/common.css"));
        assertTrue(rc.got.contains("https://secure.archive.org/img/error-icon.gif"));
        assertTrue(rc.got.contains("style1.css"));
        assertTrue(rc.got.contains("style2.css"));
        assertTrue(rc.got.contains("http://archive.org/common.css"));
        assertTrue(rc.got.contains("bg.gif"));
	}
}
