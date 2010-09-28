/* ContextAwareLexerTest
 *
 * $Id$:
 *
 * Created on Sep 10, 2010.
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.Translate;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ContextAwareLexerTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.util.htmllex.ContextAwareLexer#nextNode()}.
	 * @throws ParserException 
	 * @throws IOException 
	 */
	public void testNextNode() throws ParserException, IOException {
		String url = "http://foo.com/";
		String date = "2001";
		ConvertAccumulator ca = accumulate(url,date,"<a href=\"/boo\">boo</a>");	
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo",ca.ops.get(0).url);
		assertEquals("wrong flag","",ca.ops.get(0).context);
		assertEquals("wrong date",date,ca.ops.get(0).datespec);

		ca = accumulate(url,date,"<img src=\"/boo\"></img>");	
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo",ca.ops.get(0).url);
		assertEquals("wrong flag","im_",ca.ops.get(0).context);
		
		ca = accumulate(url,date,"<a href=\"&#32;    /boo\">boo</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo",ca.ops.get(0).url);

		ca = accumulate(url,date,"<a href=\"&#32;    /boo?foo=bar\">boo</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo?foo=bar",ca.ops.get(0).url);

		ca = accumulate(url,date,"<a href=\"&#32;    /boo?foo=bar&baz=snazz\">boo</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo?foo=bar&baz=snazz",ca.ops.get(0).url);
		
		// BUGBUG: org.htmlparer.util.Translate.decode() seems broken...
		ca = accumulate(url,date,"<a href=\"&#32;    /boo?foo=bar&lang=gang\">boo</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/boo?foo=bar&lang=gang",ca.ops.get(0).url);

		ca = accumulate(url,date,"<a href=\"&#32;    /p/s-w-%E2%80%9Ctext%E2%80%9D\">boo</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/p/s-w-%E2%80%9Ctext%E2%80%9D",ca.ops.get(0).url);
		
	}
	
	
	private void compareDecodes(String orig) {
		String htmlparserDecoded = Translate.decode(orig);
		String apacheDecoded = StringEscapeUtils.unescapeHtml(orig);
		System.out.format("ORIGINAL:(%s)\n", orig);
		System.out.format("htmlparser:(%s)\n", htmlparserDecoded);
		System.out.format("apache:(%s)\n", apacheDecoded);
	}
	
	public void testDecode() throws ParserException, IOException {
		
//		compareDecodes("/p/s-w-%E2%80%9Ctext%E2%80%9D");
//		compareDecodes("&#32;    /boo?foo=bar&lang=gang");
	
	}	
	
	private ConvertAccumulator accumulate(String base, String datespec, String html) throws IOException, ParserException {
		assertNull(null);
		Lexer lexer = new Lexer(html);
		URL url = new URL(base);
//		String datespec = "2001";
		ConvertAccumulator ca = new ConvertAccumulator();
		ReplayParseContext rpc = 
			new ReplayParseContext(new TestContextURICFactory(ca),url,datespec);

		ContextAwareLexer caLex = new ContextAwareLexer(lexer, rpc);
		ArrayList<Node> nodes = new ArrayList<Node>();
		FastArchivalUrlReplayParseEventHandler handler = 
			new FastArchivalUrlReplayParseEventHandler();
		handler.setCommentJsp(null);
		handler.setJspInsertPath(null);
		
		while(true) {
			Node next = caLex.nextNode();
			if(next == null) {
				break;
			}
			handler.handleNode(rpc, next);
			nodes.add(next);
		}
		return ca;
	}
	
	

	public class ConvertOperation {
		String context;
		String datespec;
		String url;
		public ConvertOperation(String c, String d, String u) {
			context = c;
			datespec = d;
			url = u;
		}
		public String toString() {
			return "ConvertOp:c("+context+") d("+datespec+") u("+url+")";
		}
	}
	
	public class ConvertAccumulator {
		ArrayList<ConvertOperation> ops = null;
		public ConvertAccumulator() {
			ops = new ArrayList<ContextAwareLexerTest.ConvertOperation>();
		}
		public void accumulate(String c, String d, String u) {
			ops.add(new ConvertOperation(c, d, u));
		}
	}
	
	public class TestContextURICFactory implements ContextResultURIConverterFactory {
		ConvertAccumulator ca = null;
		public TestContextURICFactory(ConvertAccumulator ca) {
			this.ca = ca;
		}
		public ResultURIConverter getContextConverter(String flags) {
			return new TestContextURIC(ca, flags);
		}
	}

	public class TestContextURIC implements ResultURIConverter {
		String context;
		ConvertAccumulator ca;
		public TestContextURIC(ConvertAccumulator ca, String context) {
			this.context = context;
			this.ca = ca;
		}
		public String makeReplayURI(String datespec, String url) {
			ca.accumulate(context, datespec, url);
			return url;
		}
	}
}
