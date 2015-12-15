/**
 *
 */
package org.archive.wayback.replay.html.transformer;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.ReplayParseContextMock;
import org.easymock.EasyMock;

/**
 * unit test for {@link BlockCSSStringTransformer}.
 *
 * @author kenji
 *
 */
public class InlineCSSStringTransformerTest extends TestCase {

	String baseURL;
	ReplayParseContextMock rpc;
	InlineCSSStringTransformer st;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		baseURL = "http://foo.com";
		rpc = new ReplayParseContextMock();
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
			EasyMock.expect(rpc.mock.contextualizeUrl(c[1], "im_")).andReturn(c[1]);
			EasyMock.replay(rpc.mock);

			st.transform(rpc, c[0]);

			EasyMock.reset(rpc.mock);
		}
	}
}
