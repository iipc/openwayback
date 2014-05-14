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
 * @author kenji
 *
 */
public class InlineCSSStringTransformerTest extends TestCase {

	URL baseURL;
	RecordingReplayParseContext rc;
	InlineCSSStringTransformer st;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		baseURL = new URL("http://foo.com");
		rc = new RecordingReplayParseContext(null, baseURL, null);
		st = new InlineCSSStringTransformer();
	}

	public void testTransform() throws Exception {
		final String[][] cases = new String[][] {
				{
					"color: #fff; background: transparent url(bg.gif)",
					"bg.gif"
				},
				{
					"background-image: url(https://secure.archive.org/grid.png);"+
						" border: 1px solid black;",
					"https://secure.archive.org/grid.png"
				},
				{
					"list-style:square url(\"//archive.org/sqred.gif\");",
					"//archive.org/sqred.gif"
				}
		};
		
		for (String[] c : cases) {
			rc = new RecordingReplayParseContext(null, baseURL, null);
			st.transform(rc, c[0]);
			assertEquals(c[0], 1, rc.got.size());
			assertEquals(c[0], c[1], rc.got.get(0));
		}
	}
	
	/**
	 * test of {@code rewriteHttpsOnly}.
	 * <p>Now it doesn't affect StringTransformer's behavior. This test will
	 * be dropped soon.</p>
	 * @throws Exception
	 */
	public void testRewriteHttpsOnly() throws Exception {
		final String[][] cases = new String[][] {
				{
					"color: #fff; background: transparent url(bg.gif)",
					"bg.gif"
				},
				{
					"background-image: url(http://archive.org/grid.png);",
					"http://archive.org/grid.png",
				},
				{
					"background-image: url(https://secure.archive.org/grid.png);"+
						" border: 1px solid black;",
					"https://secure.archive.org/grid.png"
				},
				{
					"list-style:square url(\"//archive.org/sqred.gif\");",
					"//archive.org/sqred.gif"
				}
		};

		for (String[] c : cases) {
			rc = new RecordingReplayParseContext(null, baseURL, null);
			rc.setRewriteHttpsOnly(true);
			st.transform(rc, c[0]);
			if (c[1] != null) {
				assertEquals(c[0], 1, rc.got.size());
				assertEquals(c[0], c[1], rc.got.get(0));
			} else {
				assertEquals(c[0], 0, rc.got.size());
			}
		}
	}
}
