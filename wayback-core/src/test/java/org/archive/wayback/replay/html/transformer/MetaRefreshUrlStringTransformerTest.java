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

import junit.framework.TestCase;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.html.transformer.JSStringTransformerTest.ReplayParseContextMock;
import org.easymock.EasyMock;

/**
 * @author brad
 * @author kenji
 */
public class MetaRefreshUrlStringTransformerTest extends TestCase {

	String baseURL;
	ReplayParseContextMock rpc;
	CaptureSearchResult result;
	MetaRefreshUrlStringTransformer st;

	@Override
	protected void setUp() throws Exception {
		baseURL = "http://foo.com/";
		rpc = new ReplayParseContextMock();

		st = new MetaRefreshUrlStringTransformer();
	}

	public void testTransformFull() throws Exception {
		final String input = "0; URL=https://www.example.com/content";
		EasyMock.expect(
			rpc.mock.contextualizeUrl("https://www.example.com/content", ""))
			.andReturn("https://www.example.com/content");
		EasyMock.replay(rpc.mock);

		st.transform(rpc, input);
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
			EasyMock.expect(rpc.mock.contextualizeUrl(c[1], "")).andReturn(c[1]);
			EasyMock.replay(rpc.mock);
			st.transform(rpc, c[0]);

			EasyMock.reset(rpc.mock);
		}
	}
}
