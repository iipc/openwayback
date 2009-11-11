/* ParseContextTest
 *
 * $Id$:
 *
 * Created on Nov 10, 2009.
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

package org.archive.wayback.util.htmllex;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.archive.net.UURI;
import org.archive.net.UURIFactory;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ParseContextTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.util.htmllex.ParseContext#contextualizeUrl(java.lang.String)}.
	 */
	public void testContextualizeUrl() {
		ParseContext pc = new ParseContext();
		try {
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
		
	}

}
