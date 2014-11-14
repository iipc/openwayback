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

		// path relative doesn't get contextualized:
		String url2 = "http://foo.com/bar/baz.html";
		ca = accumulate(url2,date,"<a href=\"kay\">key</a>");
		assertEquals("wrong number of rewrites",0,ca.ops.size());

		// server relative jumps to root directory:
		ca = accumulate(url2,date,"<a href=\"/kay\">key</a>");
		assertEquals("wrong number of rewrites",1,ca.ops.size());
		assertEquals("wrong URL","http://foo.com/kay",ca.ops.get(0).url);
	
		// real world example:
//		    "http://www.tn.gov/comaging/"
//			"documents/Tennessee State Plan 2009 - 2013 signed.pdf"

		
// no contextualize for path-relative		
		String url3 = "http://foo.com/bar/";
		ca = accumulate(url3,date,"<a href=\"doc/foo bar.pdf\">key</a>");
		assertEquals("wrong number of rewrites",0,ca.ops.size());
		
	}
	
	
	private void compareDecodes(String orig) {
		String htmlparserDecoded = Translate.decode(orig);
		String apacheDecoded = StringEscapeUtils.unescapeHtml(orig);
		System.out.format("ORIGINAL:(%s)\n", orig);
		System.out.format("htmlparser:(%s)\n", htmlparserDecoded);
		System.out.format("apache:(%s)\n", apacheDecoded);
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
		handler.init();
		
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
