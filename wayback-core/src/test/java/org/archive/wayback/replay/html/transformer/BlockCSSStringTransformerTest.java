/**
 *
 */
package org.archive.wayback.replay.html.transformer;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.ReplayParseContextMock;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * unit test for {@link BlockCSSStringTransformer}.
 *
 * @author kenji
 *
 */
public class BlockCSSStringTransformerTest extends TestCase {

	ReplayParseContextMock rpc;
	BlockCSSStringTransformer st;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		rpc = new ReplayParseContextMock();
		st = new BlockCSSStringTransformer();
	}

	public void testTransform() throws Exception {
		IAnswer<String> answer = new IAnswer<String>() {
			@Override
			public String answer() throws Throwable {
				return (String)EasyMock.getCurrentArguments()[0];
			}
		};
        final String input =
        		"@import \"style1.css\";\n" +
        		"@import 'style2.css';\n" +
        		"@import 'http://archive.org/common.css';\n" +
        		"BODY {\n" +
        		"  color: #fff;\n" +
        		"  background: transparent url(bg.gif);\n" +
        		"}\n";
        EasyMock.expect(rpc.mock.contextualizeUrl("style1.css", "cs_")).andAnswer(answer);
        EasyMock.expect(rpc.mock.contextualizeUrl("style2.css", "cs_")).andAnswer(answer);
        EasyMock.expect(rpc.mock.contextualizeUrl("http://archive.org/common.css", "cs_")).andAnswer(answer);
        EasyMock.expect(rpc.mock.contextualizeUrl("bg.gif", "im_")).andAnswer(answer);

        EasyMock.replay(rpc.mock);

        st.transform(rpc, input);

        EasyMock.verify(rpc.mock);
	}
}
