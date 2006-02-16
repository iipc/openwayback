/* TagMagixTest
 *
 * $Id$
 *
 * Created on 6:36:07 PM Feb 14, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TagMagixTest extends TestCase {

	/*
	 * Test method for 'org.archive.wayback.archivalurl.TagMagix.markupTag(StringBuffer, String, String, String, String, String)'
	 */
	public void testMarkupTag() {
		checkMarkup(
				"<A HREF=http://goofy.com/>",
				"<A HREF=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		checkMarkup(
				"<A HREF=\"http://goofy.com/\">",
				"<A HREF=\"http://web.archive.org/wayback/2004/http://goofy.com/\">",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		checkMarkup(
				"<A HREF='http://goofy.com/'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://goofy.com/'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");
	}

	
	private void checkMarkup(String orig, String want, String tag, String attr, String prefix, String ts, String url) {
		StringBuffer buf = new StringBuffer(orig);
		TagMagix.markupTagRE(buf,prefix,url,ts,tag,attr);
		String marked = buf.toString();
		//assertEquals(marked,want);
	}
}
