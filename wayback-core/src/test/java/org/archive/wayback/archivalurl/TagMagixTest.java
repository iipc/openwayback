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

import java.util.Properties;

import org.archive.wayback.exception.ConfigurationException;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TagMagixTest extends TestCase {
	
	// snipped and modified from http://www.sudaneseonline.com/ on 20070418...
	// note: leading space in description META content
	// note: added newlines in Content-Language META tag
	// note: no quotes around Author META content
	
	String thePage = "<html>\n" +
	"<head>\n" +
	"<meta http-equiv=\"Content-Language\" \n   content=\"ar-eg\">\n" +
	"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1256\">\n" +
	"<meta name=\"resource-type\" content=\"document\">\n" +
	"<meta name=\"classification\" content=\"News\">\n" +
	"<meta name=\"test1234\" content=\"one\ntwo\">\n" +
	"<meta name=\"description\" content=\" A voice of the Sudan people on the  Internet\">\n" +

	"<meta http-equiv=\"Content-Language\" \n   content=\"ar-sa\">\n" +
	"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1256\">\n" +
	"<META NAME=\"Author\" CONTENT=Bakri Abubakr http://bayanit.com/>\n" +
	"<META NAME=\"Author2\" CONTENT=\"Bakri Abubakr http://bayanit.com/\">\n" +
	"</head>\n" +
	"<body>foo</body>\n" +
	"</html>\n";

	/**
	 * Tests the code that finds attribute values in tags
	 */
	public void testFindAttr() {
		
		checkAttrValue(thePage,"meta","http-equiv","Content-Language");
	}
	/**
	 * 
	 */
	public void testFindAttrWhere() {
		checkAttrWhereValue(thePage,"meta","content","http-equiv",
				"Content-Type","text/html; charset=windows-1256");

		checkAttrWhereValue(thePage,"meta","content","http-equiv",
				"Content-Language","ar-eg");

		checkAttrWhereValue(thePage,"meta","content","name",
				"classification","News");

		checkAttrWhereValue(thePage,"meta","content","name",
				"test1234","one\ntwo");
		
		checkAttrWhereValue(thePage,"meta","content","name",
				"ClAsSification","News");

		checkAttrWhereValue(thePage,"meta","content","name",
				"description"," A voice of the Sudan people on the  Internet");

		checkAttrWhereValue(thePage,"meta","content","name",
				"description-no-existo",null);

		checkAttrWhereValue(thePage,"meta","content","name",
				"author","Bakri");

		checkAttrWhereValue(thePage,"meta","content","name",
				"author2","Bakri Abubakr http://bayanit.com/");
	}
	
	
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
				"LINK","HREF","http://web.archive.org/wayback/","2004","http://archive.org/dir/");
		
		// Javascript escaped quote attribute:
		checkMarkup(
				 "document.write(\"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"/css/print.css\\\" />\");",
				 "document.write(\"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"http://web.archive.org/wayback/2004/http://boogle.org/css/print.css\\\" />\");",
				"LINK","HREF","http://web.archive.org/wayback/","2004","http://boogle.org/dir/");
		
		
	}

	private void checkAttrValue(String page, String tag, String attr, 
			String wantValue) {
		StringBuilder sb = new StringBuilder(page);
		String foundValue = TagMagix.getTagAttr(sb, tag, attr);
		assertEquals(foundValue,wantValue);
	}
	private void checkAttrWhereValue(String page, String tag, String attr, 
			String whereAttr, String whereVal, String wantValue) {
		StringBuilder sb = new StringBuilder(page);
		String foundValue = TagMagix.getTagAttrWhere(sb, tag, attr, whereAttr,whereVal);
		if(foundValue != null) {
			assertEquals(foundValue,wantValue);
		} else {
			assertNull(wantValue);
		}
	}
	
	private void checkMarkup(String orig, String want, String tag, String attr, String prefix, String ts, String url) {
		StringBuilder buf = new StringBuilder(orig);
//		if(url.startsWith("http://")) {
//			url = url.substring(7);
//		}
		ArchivalUrlResultURIConverter uriC = new ArchivalUrlResultURIConverter();
		uriC.setReplayURIPrefix(prefix);
		TagMagix.markupTagREURIC(buf,uriC,ts,url,tag,attr);
		String marked = buf.toString();
		assertEquals(want,marked);
	}
}
