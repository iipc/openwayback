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

	/**
	 * Test method for 'org.archive.wayback.archivalurl.TagMagix.markupTag(StringBuffer, String, String, String, String, String)'
	 */
	public void testMarkupTag() {

		// simple simple -- no quotes at all
		checkMarkup(
				"<A HREF=http://goofy.com/>",
				"<A HREF=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// same test with lower case
		checkMarkup(
				"<a href=http://goofy.com/>",
				"<a href=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// with funky mixed case
		checkMarkup(
				"<a hREF=http://goofy.com/>",
				"<a hREF=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// more funky mixed case, this time in the attribute to replace argument
		checkMarkup(
				"<a hREF=http://goofy.com/>",
				"<a hREF=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"A","HREF","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// another funky case permutation, this time in the tagname to replace
		checkMarkup(
				"<a hREF=http://goofy.com/>",
				"<a hREF=http://web.archive.org/wayback/2004/http://goofy.com/>",
				"a","HREF","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// with double quotes
		checkMarkup(
				"<A HREF=\"http://goofy.com/\">",
				"<A HREF=\"http://web.archive.org/wayback/2004/http://goofy.com/\">",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// single quotes
		checkMarkup(
				"<A HREF='http://goofy.com/'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://goofy.com/'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// two tags
		checkMarkup(
				"<A HREF='http://goofy.com/'><A HREF='http://goofier.com/'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://goofy.com/'><A HREF='http://web.archive.org/wayback/2004/http://goofier.com/'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// two tags with newline:
		checkMarkup(
				"<A HREF='http://goofy.com/'>\n<A HREF='http://goofier.com/'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://goofy.com/'>\n<A HREF='http://web.archive.org/wayback/2004/http://goofier.com/'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		
		// two tags in "page" but only asking to update one of them
		checkMarkup(
				"<A HREF='http://goofy.com/'><B HREF='http://goofier.com/'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://goofy.com/'><B HREF='http://goofier.com/'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");
	
		// two tags, asking to update the other.
		checkMarkup(
				"<A HREF='http://goofy.com/'><B HREF='http://goofier.com/'>",
				"<A HREF='http://goofy.com/'><B HREF='http://web.archive.org/wayback/2004/http://goofier.com/'>",
				"B","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// simple path relative
		checkMarkup(
				"<A HREF='index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// simple server relative but irrelavant -- still at top level
		checkMarkup(
				"<A HREF='/index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/");

		// server relative but with non directory base url
		checkMarkup(
				"<A HREF='/index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir");

		// server relative being significant
		checkMarkup(
				"<A HREF='/index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// path relative with non-directory base url
		checkMarkup(
				"<A HREF='index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir");

		// path relative in subdirectory
		checkMarkup(
				"<A HREF='index.html'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/dir/index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// don't touch a "malformed" attribute (no closing apos)
		checkMarkup(
				"<A HREF='index.html>",
				"<A HREF='index.html>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// don't touch a "malformed" attribute (no differing quotes around attribute.)
		checkMarkup(
				"<A HREF='index.html\">",
				"<A HREF='index.html\">",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// same as last, but reversed: don't touch a "malformed" attribute (no differing quotes around attribute.)
		checkMarkup(
				"<A HREF=\"index.html'>",
				"<A HREF=\"index.html'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// newline in attribute
		checkMarkup(
				"<A HREF='/index.html'\n FOO='bar'>",
				"<A HREF='http://web.archive.org/wayback/2004/http://www.archive.org/index.html'\n FOO='bar'>",
				"A","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// newlines in attribute
		checkMarkup(
				"<link rel=\"stylesheet\"\n goo=\"1\"\n href=\"/_style/style.css\">",
				"<link rel=\"stylesheet\"\n goo=\"1\"\n href=\"http://web.archive.org/wayback/2004/http://www.archive.org/_style/style.css\">",
				"link","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");
		
		// newlines in attribute, plus extra
		checkMarkup(
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"/_style/style.css\"></b>",
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"http://web.archive.org/wayback/2004/http://www.archive.org/_style/style.css\"></b>",
				"link","href","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// newlines in attribute, plus extra, diff case
		checkMarkup(
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"/_style/style.css\"></b>",
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"http://web.archive.org/wayback/2004/http://www.archive.org/_style/style.css\"></b>",
				"LINK","HREF","http://web.archive.org/wayback/","2004","http://www.archive.org/dir/");

		// newlines in attribute, plus extra, diff case, no protocol
		checkMarkup(
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"/_style/style.css\"></b>",
				"<b><link rel=\"stylesheet\"\n goo=\"1\"\n href=\"http://web.archive.org/wayback/2004/http://archive.org/_style/style.css\"></b>",
				"LINK","HREF","http://web.archive.org/wayback/","2004","archive.org/dir/");
		
	}

	
	private void checkMarkup(String orig, String want, String tag, String attr, String prefix, String ts, String url) {
		StringBuffer buf = new StringBuffer(orig);
		TagMagix.markupTagRE(buf,prefix,url,ts,tag,attr);
		String marked = buf.toString();
		assertEquals(marked,want);
	}
}
