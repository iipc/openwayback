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
        assertEquals("style1.css", rc.got.get(0));
        assertEquals("style2.css", rc.got.get(1));
        assertEquals("http://archive.org/common.css", rc.got.get(2));
        assertEquals("bg.gif", rc.got.get(3));
	}
	
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
        
        assertEquals(2, rc.got.size());
        assertEquals("https://secure.archive.org/common.css", rc.got.get(0));
        assertEquals("https://secure.archive.org/img/error-icon.gif", rc.got.get(1));
	}
}
