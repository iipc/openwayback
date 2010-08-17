/* JSStringTransformerTest
 *
 * $Id$:
 *
 * Created on Dec 10, 2009.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
