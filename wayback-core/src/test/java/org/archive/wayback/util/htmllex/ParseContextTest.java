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
package org.archive.wayback.util.htmllex;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.htmlparser.util.Translate;

/**
 * @author brad
 *
 */
public class ParseContextTest extends TestCase {

	/**
	 * test of {@link Translate#decode(String)}
	 * Note: we no longer use this method for decoding entities.
	 */
	public void testTranslate() {
		String orig = "http://foo.com/main?arg1=1&lang=2";
		String xlated = Translate.decode(orig);
		System.out.format("Orig(%s) xlated(%s)\n",orig,xlated);
		String orig2 = "&#32;               gaz.cgi?foo=bar&lang=2";
		String xlated2 = Translate.decode(orig2);
		System.out.format("Orig2(%s) xlated2(%s)\n",orig2,xlated2);
	}
	
	/**
	 * Test method for {@link org.archive.wayback.util.htmllex.ParseContext#contextualizeUrl(java.lang.String)}.
	 */
	public void testContextualizeUrl() throws URISyntaxException, MalformedURLException {
		ParseContext pc = new ParseContext();

		URI tmp = new URI("http://base.com/foo.html#REF");
		String ref = tmp.getFragment();
		assertEquals("REF",ref);
		tmp = new URI("http://base.com/foo.html");
		assertNull(tmp.getFragment());

		pc.setBaseUrl(new URL("http://base.com/"));
		assertEquals("http://base.com/images.gif",
				pc.contextualizeUrl("/images.gif"));
		assertEquals("http://base.com/images.gif",
				pc.contextualizeUrl("../images.gif"));
		assertEquals("http://base.com/images.gif",
				pc.contextualizeUrl("../../images.gif"));
		assertEquals("http://base.com/image/1s.gif",
				pc.contextualizeUrl("/image/1s.gif"));
		assertEquals("http://base.com/image/1s.gif",
				pc.contextualizeUrl("../../image/1s.gif"));
		assertEquals("http://base.com/image/1s.gif",
				pc.contextualizeUrl("/../../image/1s.gif"));
		assertEquals("http://base.com/image/1.html#REF",
				pc.contextualizeUrl("/../../image/1.html#REF"));
		assertEquals("http://base.com/image/1.html#REF FOO",
				pc.contextualizeUrl("/../../image/1.html#REF FOO"));
		assertEquals("http://base.com/image/foo?boo=baz",
				pc.contextualizeUrl("/image/foo?boo=baz"));

		assertEquals("http://base.com/image/foo?boo=baz%3A&gar=war",
				pc.contextualizeUrl("/image/foo?boo=baz%3A&gar=war"));
	}

	public void testResolve() throws Exception {

		ParseContext pc = new ParseContext();
		URL base = new URL("http://foo.com/dir/bar.html#REF");
			
		pc.setBaseUrl(base);
		checkRes(pc,"http://foo.com/images.gif","/images.gif");
		checkRes(pc,"http://foo.com/dir/images.gif","images.gif");
		checkRes(pc,"http://foo.com/dir/images.gif","./images.gif");
		checkRes(pc,"http://foo.com/images.gif","../images.gif");
		checkRes(pc,"http://foo.com/dir/images.gif","../dir/images.gif");
		checkRes(pc,"http://foo.com/dir2/images.gif","../dir2/images.gif");
		checkRes(pc,"http://foo.com/dir/dir2/images.gif","dir2/images.gif");
		checkRes(pc,"http://foo.com/im/images.gif","/im/images.gif");
		checkRes(pc,"http://foo.com/im/images.gif","/im/images.gif   ");

		checkRes(pc,"http://foo.com/dir/images.gif","    images.gif");

		checkRes(pc,"http://foo.com/dir/images.gif#NAME","     images.gif#NAME");
		checkRes(pc,"http://foo.com/dir/images.gif#NAME","     images.gif  #NAME");

		
		checkRes(pc,"http://foo.com/%20im.gif","/ im.gif");
		checkRes(pc,"http://foo.com/%20%20im.gif","/  im.gif");
		checkRes(pc,"http://foo.com/%20%20im.gif","/  im.gif ");
	}

	private void checkRes(ParseContext pc, String want, String rel) throws URISyntaxException {
		assertEquals(want, pc.resolve(rel));
	}
}
