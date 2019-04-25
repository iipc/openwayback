/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.replay.html.transformer;

import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

/**
 * unit test for {@link SrcsetStringTransformer}.
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

    public void testTransformWithCommas() throws Exception {
        final String[] c = new String[] {
            "a,b,c,,, /images/foo1.jpg 800w,data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7 700w, /images/foo2.jpg 480w(data:,foo, ,), /images/foo3.jpg 96w(x ",
            "a,b,c",
            "/images/foo1.jpg",
            "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7",
            "/images/foo2.jpg",
            "/images/foo3.jpg"
        };

        rc = new RecordingReplayParseContext(null, baseURL, null);
        st.transform(rc, c[0]);
        assertEquals(c[0], 5, rc.got.size());
        assertEquals(c[0], c[1], rc.got.get(0));
        assertEquals(c[0], c[2], rc.got.get(1));
        assertEquals(c[0], c[3], rc.got.get(2));
        assertEquals(c[0], c[4], rc.got.get(3));
        assertEquals(c[0], c[5], rc.got.get(4));
    }
}
