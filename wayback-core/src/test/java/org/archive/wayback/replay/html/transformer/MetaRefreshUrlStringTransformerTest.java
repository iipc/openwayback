/* MetaRefreshUrlStringTransformerTest
 *
 * $Id$:
 *
 * Created on Jan 12, 2010.
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
