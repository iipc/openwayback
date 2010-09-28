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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class JSStringTransformerTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.replay.html.transformer.JSStringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)}.
	 * @throws MalformedURLException 
	 */
	public void testTransform() throws MalformedURLException {
		RecordingReplayParseContext rc = new RecordingReplayParseContext(null, new URL("http://foo.com/"), null);
		String input = "'<a href=\'http://www.gavelgrab.org\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		JSStringTransformer jst = new JSStringTransformer();
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http://www.gavelgrab.org",rc.got.get(0));

		input = "'<a href=\'http://www.gavelgrab.org/foobla/blah\' target=\'_blank\'>Learn more in Gavel Grab</a>'";
		rc = new RecordingReplayParseContext(null, new URL("http://foo.com/"), null);
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http://www.gavelgrab.org",rc.got.get(0));

		input = "onloadRegister(function (){window.location.href=\"http:\\/\\/www.facebook.com\\/barrettforwisconsin?v=info\";});";
		rc = new RecordingReplayParseContext(null, new URL("http://foo.com/"), null);
		jst.transform(rc, input);
		assertEquals(1,rc.got.size());
		assertEquals("http:\\/\\/www.facebook.com",rc.got.get(0));
		
	}

	public class RecordingReplayParseContext extends ReplayParseContext {
		ArrayList<String> got = null;
		/**
		 * @param uriConverterFactory
		 * @param baseUrl
		 * @param datespec
		 */
		public RecordingReplayParseContext(
				ContextResultURIConverterFactory uriConverterFactory,
				URL baseUrl, String datespec) {
			super(uriConverterFactory, baseUrl, datespec);
			got = new ArrayList<String>();
			// TODO Auto-generated constructor stub
		}
		public String contextualizeUrl(String url) {
			got.add(url);
			return url;
		}
		
	}
}
