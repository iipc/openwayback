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

/**
 * @author brad
 *
 */
public class MetaRefreshUrlStringTransformerTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.replay.html.transformer.MetaRefreshUrlStringTransformer#transform(org.archive.wayback.replay.html.ReplayParseContext, java.lang.String)}.
	 */
	public void testTransform() {
//		cmpT("0; url=http://foo.com/bar","0; url=(((http://foo.com/bar)))");
//		cmpT("0; url=/bar","0; url=(((/bar)))");
//		cmpT("0; url =/bar","0; url =(((/bar)))");
//		cmpT("0;  url =/bar","0;  url =(((/bar)))");
//		cmpT(";  url =/bar",";  url =/bar");
//		cmpT("0;  URL =/bar","0;  URL =(((/bar)))");
//
//		cmpT("0; URL = /bar","0; URL = (((/bar)))");
//		cmpT("0; URL = /bar ","0; URL = (((/bar))) ");
//		cmpT("0; URL = /bar   ","0; URL = (((/bar)))   ");
//		cmpT("0; URL = /baz foo","0; URL = (((/baz foo)))");
//		cmpT("0; URL = /baz foo ","0; URL = (((/baz foo))) ");
//		cmpT("0; URL=/baz foo ","0; URL=(((/baz foo))) ");
//
//		cmpT("0; UrL=/baz foo ","0; UrL=(((/baz foo))) ");
//		cmpT("0; UrL=/baZefoo ","0; UrL=(((/baZefoo))) ");
		
	}
	private void cmpT(String source, String want) {
		MetaRefreshUrlStringTransformer m = new MetaRefreshUrlStringTransformer();
		String got = m.transform(null,source);
		assertEquals(want, got);
	}

}
