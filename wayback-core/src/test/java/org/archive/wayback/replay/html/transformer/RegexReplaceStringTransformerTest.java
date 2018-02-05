/*
 *
 */
package org.archive.wayback.replay.html.transformer;

import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

/**
 * unit test for {@link RegexReplaceStringTransformer}.
 *
 */
public class RegexReplaceStringTransformerTest extends TestCase {

    URL baseURL;
    RecordingReplayParseContext rc;
    RegexReplaceStringTransformer st;

    /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
    protected void setUp() throws Exception {
        baseURL = new URL("http://foo.com");
        rc = new RecordingReplayParseContext(null, baseURL, null);
        }

    public void testTransform() throws Exception {
        // values are input, regex to find, replacement for regex, expected result
        final String[][] cases = new String[][] {
            {
                "sha256-1LpauPYMnfXHM+fTx8Rp/5Pca2TSsqj+uUr6j3fGYZM=",
                ".*sha256.*",
                "",
                "",
            },
            {
                "examples/video/test.mp4",
                "video/[a-z]*.mp4",
                "video/changed.mp4",
                "examples/video/changed.mp4",
            }
        };

        for (String[] c : cases) {
            st = new RegexReplaceStringTransformer(c[1], c[2]);
            String result = st.transform(rc, c[0]);
            assertEquals(c[3], result);
        }
    }
}
