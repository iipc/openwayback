package org.archive.wayback.archivalurl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ContextAwareLexer;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

import junit.framework.TestCase;

public class FastArchivalUrlReplayParseEventHandlerTest extends TestCase {
	
	
	

	public void testRewrite() throws Exception {
		assertEquals("<html><a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">foo</a></html>",doEndToEnd("<html><a href=\"/foo.html\">foo</a></html>"));
		assertEquals("<html><a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">foo</a></html>",doEndToEnd("<html><a href=\"foo.html\">foo</a></html>"));
		assertEquals("<html><a href=\"javascript:doWin('http://replay.archive.org/2001/http://www.symphony.org')\">American Symphony Orchestra League</a></html>",doEndToEnd("<html><a href=\"javascript:doWin('http://www.symphony.org')\">American Symphony Orchestra League</a></html>"));
	}
	
	public String doEndToEnd(String input) throws Exception {
		String baseUrl = "http://www.example.com/";
		String timestamp = "2001";
		String outputCharset = "UTF-8";
		String charSet = "UTF-8";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(charSet));
		
		FastArchivalUrlReplayParseEventHandler delegator = new FastArchivalUrlReplayParseEventHandler();
		delegator.setCommentJsp(null);
		delegator.setJspInsertPath(null);
		
		ArchivalUrlResultURIConverter uriConverter = new ArchivalUrlResultURIConverter();
		uriConverter.setReplayURIPrefix("http://replay.archive.org/");
		
		ArchivalUrlContextResultURIConverterFactory fact = 
			new ArchivalUrlContextResultURIConverterFactory(
					(ArchivalUrlResultURIConverter) uriConverter);

		// The URL of the page, for resolving in-page relative URLs: 
    	URL url = null;
		try {
			url = new URL(baseUrl);
		} catch (MalformedURLException e1) {
			// TODO: this shouldn't happen...
			e1.printStackTrace();
			throw new IOException(e1.getMessage());
		}

		// To make sure we get the length, we have to buffer it all up...
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// set up the context:
		ReplayParseContext context = 
			new ReplayParseContext(fact,url,timestamp);
		context.setOutputCharset(outputCharset);
		context.setOutputStream(baos);
		context.setJspExec(null);
		
		// and finally, parse, using the special lexer that knows how to
		// handle javascript blocks containing unescaped HTML entities:
		Page lexPage = new Page(bais,charSet);
		Lexer lexer = new Lexer(lexPage);
		Lexer.STRICT_REMARKS = false;
    	ContextAwareLexer lex = new ContextAwareLexer(lexer, context);
    	Node node;
    	try {
			while((node = lex.nextNode()) != null) {
				delegator.handleNode(context, node);
			}
			delegator.handleParseComplete(context);
		} catch (ParserException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		// At this point, baos contains the utf-8 encoded bytes of our result:
		return new String(baos.toByteArray(),outputCharset);

	}
}
