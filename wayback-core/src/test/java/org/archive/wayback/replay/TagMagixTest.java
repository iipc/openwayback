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
package org.archive.wayback.replay;

import org.archive.wayback.replay.TagMagix;
import org.archive.wayback.archivalurl.ArchivalUrlReplayURIConverter;

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
	
	public void testFindEndOfFirst() {
		findEndOf("<head>","head",6);
		findEndOf("<html><head><body>","head",12);
		findEndOf("<html><head goo=bar><body>","head",20);
		findEndOf("<html><head goo=bar><body>full","body",26);
		findEndOf("<html><head goo=bar><body >full","body",27);
		findEndOf("<html><head goo=bar><body >full","body",27);
		findEndOf("<html><head goo=bar><body yar=bam>full","body",34);
		findEndOf("<html><head goo=bar><body yar='bam'>full","body",36);
		findEndOf("<html><head goo=bar><body yar=\"bam\">full","body",36);
	}
	
	public void findEndOf(String page, String tag, int offset) {
		StringBuilder sb = new StringBuilder(page);
		int found = TagMagix.getEndOfFirstTag(sb,tag);
		assertEquals("FAILED find end of " +tag+ " in ("+page+")",offset,found);
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
	
	public void testCSSMarkup() {

		// basic, with quot apos + raw:
		checkCSSMarkup("@import url(http://foo.com/f.css);",
				"@import url(http://web.archive.org/wayback/2004/http://foo.com/f.css);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url('http://foo.com/f.css');",
				"@import url('http://web.archive.org/wayback/2004/http://foo.com/f.css');",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url(\"http://foo.com/f.css\");",
				"@import url(\"http://web.archive.org/wayback/2004/http://foo.com/f.css\");",
				"http://web.archive.org/wayback/","2004","http://foo.com/");


		// same as basic, but with extra whitespace after "url"
		checkCSSMarkup("@import url (http://foo.com/f.css);",
				"@import url (http://web.archive.org/wayback/2004/http://foo.com/f.css);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url\t('http://foo.com/f.css');",
				"@import url\t('http://web.archive.org/wayback/2004/http://foo.com/f.css');",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url\n(\"http://foo.com/f.css\");",
				"@import url\n(\"http://web.archive.org/wayback/2004/http://foo.com/f.css\");",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		// whitespace within url spec:
		checkCSSMarkup("@import url( http://foo.com/f.css);",
				"@import url( http://web.archive.org/wayback/2004/http://foo.com/f.css);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url('http://foo.com/f.css' );",
				"@import url('http://web.archive.org/wayback/2004/http://foo.com/f.css' );",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url( \"http://foo.com/f.css\" );",
				"@import url( \"http://web.archive.org/wayback/2004/http://foo.com/f.css\" );",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url(\t\"http://foo.com/f.css\"\t);",
				"@import url(\t\"http://web.archive.org/wayback/2004/http://foo.com/f.css\"\t);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url(\n\"http://foo.com/f.css\"\n);",
				"@import url(\n\"http://web.archive.org/wayback/2004/http://foo.com/f.css\"\n);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import url(\r\n\"http://foo.com/f.css\"\n\r);",
				"@import url(\r\n\"http://web.archive.org/wayback/2004/http://foo.com/f.css\"\n\r);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("@import \"http://foo.com/f.css\";",
				"@import \"http://web.archive.org/wayback/2004/http://foo.com/f.css\";",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		checkCSSMarkup("@import 'http://foo.com/f.css';",
				"@import 'http://web.archive.org/wayback/2004/http://foo.com/f.css';",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("@import \"http://foo.com/f.css\"; @import url( http://foo.com/f.css);",
				"@import \"http://web.archive.org/wayback/2004/http://foo.com/f.css\"; @import url( http://web.archive.org/wayback/2004/http://foo.com/f.css);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("@import \"http://foo.com/f.css\";\n@import url( http://foo.com/f.css);",
				"@import \"http://web.archive.org/wayback/2004/http://foo.com/f.css\";\n@import url( http://web.archive.org/wayback/2004/http://foo.com/f.css);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("@import url( http://foo.com/f.css);\n@import \"http://foo.com/f.css\";",
				"@import url( http://web.archive.org/wayback/2004/http://foo.com/f.css);\n@import \"http://web.archive.org/wayback/2004/http://foo.com/f.css\";",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("background: #9caad1 url('/~alabama/images/bg.jpg') 0 0 repeat-y;",
				"background: #9caad1 url('http://web.archive.org/wayback/2004/http://foo.com/~alabama/images/bg.jpg') 0 0 repeat-y;",
				"http://web.archive.org/wayback/","2004","http://foo.com/");

		checkCSSMarkup("background: #9caad1 url('/~alabama/images/bg.jpg') 0 0 repeat-y;",
				"background: #9caad1 url('http://web.archive.org/wayback/2004/http://foo.com/~alabama/images/bg.jpg') 0 0 repeat-y;",
				"http://web.archive.org/wayback/","2004","http://foo.com/b/");

		// url path with "()" in path
		checkStyleUrlMarkup("background: #9caad1 url(/css/b(foo).gif) 0 0 repeat-y;",
				"background: #9caad1 url(http://w.a.org/wb/2004/http://f.au/css/b(foo).gif) 0 0 repeat-y;",
				"http://w.a.org/wb/","2004","http://f.au/");

		// url path with "()" in path and compressed with other rules
		checkStyleUrlMarkup("h1{background: #9caad1 url('/css/b(foo).gif')}p{display:block}",
				"h1{background: #9caad1 url('http://w.a.org/wb/2004/http://f.au/css/b(foo).gif')}p{display:block}",
				"http://w.a.org/wb/","2004","http://f.au/");

		// url path with "()" in path and first in list
		checkStyleUrlMarkup("h1{background: #9caad1 url('/css/b(foo).gif'),default !important}p{display:block}",
				"h1{background: #9caad1 url('http://w.a.org/wb/2004/http://f.au/css/b(foo).gif'),default !important}p{display:block}",
				"http://w.a.org/wb/","2004","http://f.au/");

		// url path with "()" in path and marked !important
		checkStyleUrlMarkup("h1{background: #9caad1 url('/css/b(foo).gif')!important}p{display:block}",
				"h1{background: #9caad1 url('http://w.a.org/wb/2004/http://f.au/css/b(foo).gif')!important}p{display:block}",
				"http://w.a.org/wb/","2004","http://f.au/");

		// don't convert @namespace urls
		checkCSSMarkup("@namespace url(\r\n\"http://www.w3.org/1999/xhtml\"\n\r);",
				"@namespace url(\r\n\"http://www.w3.org/1999/xhtml\"\n\r);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");
		
		checkCSSMarkup("@namespace xyz url(\r\n\"http://www.w3.org/1999/xhtml\"\n\r);",
				"@namespace xyz url(\r\n\"http://www.w3.org/1999/xhtml\"\n\r);",
				"http://web.archive.org/wayback/","2004","http://foo.com/");		
		
	}
	
	public void testStyleUrlMarkup() {
		// simple, server relative
		checkStyleUrlMarkup("<table style=\"background: url(/css/b.gif)\"></table>",
				"<table style=\"background: url(http://w.a.org/wb/2004/http://f.au/css/b.gif)\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/");
		// server-relative, which now means something
		checkStyleUrlMarkup("<table style=\"background: url(/css/b.gif)\"></table>",
				"<table style=\"background: url(http://w.a.org/wb/2004/http://f.au/css/b.gif)\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/b/");

		// path relative:
		checkStyleUrlMarkup("<table style=\"background: url(css/b.gif)\"></table>",
				"<table style=\"background: url(http://w.a.org/wb/2004/http://f.au/css/b.gif)\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// path relative, meaningful:
		checkStyleUrlMarkup("<table style=\"background: url(css/b.gif)\"></table>",
				"<table style=\"background: url(http://w.a.org/wb/2004/http://f.au/b/css/b.gif)\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/b/");

		// absolute:
		checkStyleUrlMarkup("<table style=\"background: url(http://e.au/css/b.gif)\"></table>",
				"<table style=\"background: url(http://w.a.org/wb/2004/http://e.au/css/b.gif)\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/b/");

		// apos attribute
		checkStyleUrlMarkup("<table style='background: url(/css/b.gif)'></table>",
				"<table style='background: url(http://w.a.org/wb/2004/http://f.au/css/b.gif)'></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// url path with "()" in path
		checkStyleUrlMarkup("<table style='background: url(/css/b(foo).gif)'></table>",
				"<table style='background: url(http://w.a.org/wb/2004/http://f.au/css/b(foo).gif)'></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// quote attribute, apos url:
		checkStyleUrlMarkup("<table style=\"background: url('/css/b.gif')\"></table>",
				"<table style=\"background: url('http://w.a.org/wb/2004/http://f.au/css/b.gif')\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// apos attribute, quote url:
		checkStyleUrlMarkup("<table style='background: url(\"/css/b.gif\")'></table>",
				"<table style='background: url(\"http://w.a.org/wb/2004/http://f.au/css/b.gif\")'></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// apos attribute, quote url, plus semi-colon:
		checkStyleUrlMarkup("<table style='background: url(\"/css/b.gif\");'></table>",
				"<table style='background: url(\"http://w.a.org/wb/2004/http://f.au/css/b.gif\");'></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// Two url()s in same attribute value:
		checkStyleUrlMarkup("<table style=\"bg: url(/css/b.gif); fg: url(/css/f.gif);\"></table>",
				"<table style=\"bg: url(http://w.a.org/wb/2004/http://f.au/css/b.gif); fg: url(http://w.a.org/wb/2004/http://f.au/css/f.gif);\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// Two url()s in same quoted attribute value, with embedded apos:
		checkStyleUrlMarkup("<table style=\"bg: url('/css/b.gif'); fg: url('/css/f.gif');\"></table>",
				"<table style=\"bg: url('http://w.a.org/wb/2004/http://f.au/css/b.gif'); fg: url('http://w.a.org/wb/2004/http://f.au/css/f.gif');\"></table>",
				"http://w.a.org/wb/","2004","http://f.au/");

		// Two url()s in same apos'ed attribute value, with embedded quote:
		checkStyleUrlMarkup("<table style='bg: url(\"/css/b.gif\"); fg: url(\"/css/f.gif\");'></table>",
				"<table style='bg: url(\"http://w.a.org/wb/2004/http://f.au/css/b.gif\"); fg: url(\"http://w.a.org/wb/2004/http://f.au/css/f.gif\");'></table>",
				"http://w.a.org/wb/","2004","http://f.au/");
//
//		NOT WORKING YET... Let's see if we get a complaint... Not even sure this
//		is legit HTML:
//		
//		// Two url()s in same quoted attribute value, with embedded escaped quote:
//		checkStyleUrlMarkup("<table style=\"bg: url(\\\"/css/b.gif\\\"); fg: url(\\\"/css/f.gif\\\");\"></table>",
//				"<table style=\"bg: url(\\\"http://w.a.org/wb/2004/http://f.au/css/b.gif\\\"); fg: url(\\\"http://w.a.org/wb/2004/http://f.au/css/f.gif\\\");\"></table>",
//				"http://w.a.org/wb/","2004","http://f.au/");
		
		
		checkStyleUrlMarkup("<td style=\"b-i:url(i/b.jpg);\n\"></td>",
				"<td style=\"b-i:url(http://w.a.org/wb/2004/http://f.au/i/b.jpg);\n\"></td>",
				"http://w.a.org/wb/","2004","http://f.au/");
		
//		    "<td style=\"background-image:url(images/banner.jpg);\n\"></td>"

	
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
	
	private void checkCSSMarkup(String orig, String want,String prefix, String ts, String url) {
		StringBuilder buf = new StringBuilder(orig);
		ArchivalUrlReplayURIConverter uriC = new ArchivalUrlReplayURIConverter();
		uriC.setReplayURIPrefix(prefix);
		TagMagix.markupCSSImports(buf, uriC, ts, url);
		TagMagix.markupStyleUrls(buf,uriC,ts,url);
		String marked = buf.toString();
		assertEquals(want,marked);
	}
	
	private void checkStyleOnlyUrlMarkup(String orig, String want, String prefix, String ts, String url) {
		StringBuilder buf = new StringBuilder(orig);
		ArchivalUrlReplayURIConverter uriC = new ArchivalUrlReplayURIConverter();
		uriC.setReplayURIPrefix(prefix);
		TagMagix.markupStyleUrls(buf,uriC,ts,url);
		String marked = buf.toString();
		assertEquals(want,marked);
	}

	private void checkStyleUrlMarkup(String orig, String want,String prefix, String ts, String url) {
		StringBuilder buf = new StringBuilder(orig);
		ArchivalUrlReplayURIConverter uriC = new ArchivalUrlReplayURIConverter();
		uriC.setReplayURIPrefix(prefix);
		TagMagix.markupCSSImports(buf, uriC, ts, url);
		TagMagix.markupStyleUrls(buf, uriC, ts, url);
		String marked = buf.toString();
		assertEquals(want,marked);
	}
	
	private void checkMarkup(String orig, String want, String tag, String attr, String prefix, String ts, String url) {
		StringBuilder buf = new StringBuilder(orig);
		ArchivalUrlReplayURIConverter uriC = new ArchivalUrlReplayURIConverter();
		uriC.setReplayURIPrefix(prefix);
		TagMagix.markupTagREURIC(buf,uriC,ts,url,tag,attr);
		String marked = buf.toString();
		assertEquals(want,marked);
	}
}
