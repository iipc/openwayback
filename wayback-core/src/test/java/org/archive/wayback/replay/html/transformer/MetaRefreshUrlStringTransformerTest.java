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

import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.RecordingReplayParseContext;

import junit.framework.TestCase;

/**
 * @author brad
 * @author kenji
 */
public class MetaRefreshUrlStringTransformerTest extends TestCase {

	URL baseURL;
	RecordingReplayParseContext rc;
	MetaRefreshUrlStringTransformer st;

	@Override
	protected void setUp() throws Exception {
		baseURL = new URL("http://foo.com/");
		rc = new RecordingReplayParseContext(null, baseURL, null);
		st = new MetaRefreshUrlStringTransformer();
	}

	public void testTransformFull() throws Exception {
		final String input = "0; URL=https://www.example.com/content";
		st.transform(rc, input);
		
		assertEquals(1, rc.got.size());
		assertEquals("https://www.example.com/content", rc.got.get(0));
	}

	public void testTransformVariations() throws Exception {
		final String[][] cases = new String[][] {
				{ "0; url=/bar", "/bar" },
				{ "10; url =/bar", "/bar" },
				{ "2;url=/bar", "/bar" },
				// do we want to allow this?
				//{ "; url=/bar", "/bar" },
				{ "0; URL=/bar", "/bar" },
		};
		for (String[] c : cases) {
			rc = new RecordingReplayParseContext(null, baseURL, null);
			st.transform(rc, c[0]);
			assertEquals(c[0], 1, rc.got.size());
			assertEquals(c[0], c[1], rc.got.get(0));
		}
	}

	public void testRewriteHttpsOnly() throws Exception {
		rc.setRewriteHttpsOnly(true);

		final String input = "0; URL=https://www.example.com/content";
		st.transform(rc, input);

		assertEquals(1, rc.got.size());
		assertEquals("https://www.example.com/content", rc.got.get(0));
	}
}
