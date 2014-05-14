package org.archive.wayback.archivalurl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ContextAwareLexer;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

public class FastArchivalUrlReplayParseEventHandlerTest extends TestCase {

	public void testAnchorHrefAbsolute() throws Exception {
		assertEquals("<html><a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">foo</a></html>",
		        doEndToEnd("<html><a href=\"/foo.html\">foo</a></html>"));
	}
	public void testAnchorHrefRelative() throws Exception {
		assertEquals("<html><a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">foo</a></html>",
		        doEndToEnd("<html><a href=\"foo.html\">foo</a></html>"));
	}
	public void testAnchorHrefAbsoluteInJavascript() throws Exception {
		assertEquals("<html><a href=\"javascript:doWin('http://replay.archive.org/2001/http://www.symphony.org')\">American Symphony Orchestra League</a></html>",
		        doEndToEnd("<html><a href=\"javascript:doWin('http://www.symphony.org')\">American Symphony Orchestra League</a></html>"));
	}
	
	public void testStyleElementBackgroundUrl() throws Exception {
	    final String input = "<html>" +
	    		"<head>" +
	    		"<style type=\"text/css\">" +
	    		"#head{" +
	    		"background:transparent url(/images/logo.jpg);" +
	    		"}" +
	    		"</style>" +
	    		"</head>" +
	    		"</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "#head{" +
                "background:transparent url(http://replay.archive.org/2001im_/http://www.example.com/images/logo.jpg);" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
	    assertEquals(expected, doEndToEnd(input));
	}
	
	public void testStyleElementImportUrl() throws Exception {
        final String input = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "@import \"style1.css\";\n" +
                "@import \'style2.css\';\n" +
                "@import 'http://archive.org/common.css';\n" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "@import \"http://replay.archive.org/2001cs_/http://www.example.com/style1.css\";\n" +
                "@import 'http://replay.archive.org/2001cs_/http://www.example.com/style2.css\';\n" +
                "@import 'http://replay.archive.org/2001cs_/http://archive.org/common.css';\n" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
        assertEquals(expected, doEndToEnd(input));
	    
	}
	
    public void testStyleElementFontfaceSrcUrl() throws Exception {
        // font data is not an image technically, but it'd require more elaborate
        // pattern match to differentiate a context of url function. use im_ for
        // font data for now.
        final String input = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "@font-face {" +
                "font-family: 'TestFont" +
                "src: local('TestFont')" +
                "src: url(/fonts/TestFont.otf)" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "@font-face {" +
                "font-family: 'TestFont" +
                "src: local('TestFont')" +
                "src: url(http://replay.archive.org/2001im_/http://www.example.com/fonts/TestFont.otf)" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
        assertEquals(expected, doEndToEnd(input));
    }

    public String doEndToEnd(String input) throws Exception {
		final String baseUrl = "http://www.example.com/";
		final String timestamp = "2001";
		final String outputCharset = "UTF-8";
		final String charSet = "UTF-8";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(charSet));
		
		FastArchivalUrlReplayParseEventHandler delegator = new FastArchivalUrlReplayParseEventHandler();
		delegator.setEndJsp(null);
		delegator.setJspInsertPath(null);
		
		ArchivalUrlResultURIConverter uriConverter = new ArchivalUrlResultURIConverter();
		uriConverter.setReplayURIPrefix("http://replay.archive.org/");
		
		ArchivalUrlContextResultURIConverterFactory fact = 
			new ArchivalUrlContextResultURIConverterFactory(
					(ArchivalUrlResultURIConverter) uriConverter);

		// The URL of the page, for resolving in-page relative URLs: 
    	URL url = new URL(baseUrl);

		// To make sure we get the length, we have to buffer it all up...
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// set up the context:
        ReplayParseContext context = new ReplayParseContext(fact, url,
                timestamp);
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
    	while((node = lex.nextNode()) != null) {
    	    delegator.handleNode(context, node);
    	}
    	delegator.handleParseComplete(context);

		// At this point, baos contains the utf-8 encoded bytes of our result:
		return new String(baos.toByteArray(),outputCharset);

	}
}
