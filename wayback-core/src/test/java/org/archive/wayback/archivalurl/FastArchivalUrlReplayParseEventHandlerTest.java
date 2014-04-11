package org.archive.wayback.archivalurl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ContextAwareLexer;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;

/**
 * test {@link FastArchivalUrlReplayParseEventHandler}.
 *
 */
public class FastArchivalUrlReplayParseEventHandlerTest extends TestCase {

	FastArchivalUrlReplayParseEventHandler delegator;

	@Override
	protected void setUp() throws Exception {
		delegator = new FastArchivalUrlReplayParseEventHandler();
		delegator.setEndJsp(null);
		delegator.setJspInsertPath(null);
	}

	public void testAnchorHrefAbsolute() throws Exception {
		final String input = "<html>" +
				"<a href=\"/foo.html\">foo</a>" +
				"</html>";
		final String expected = "<html>" +
				"<a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">" +
				"foo</a></html>";
		assertEquals(expected, doEndToEnd(input));
	}

	public void testAnchorHrefRelative() throws Exception {
		final String input = "<html>" +
				"<a href=\"foo.html\">foo</a>" +
				"</html>";
		final String expected = "<html>" +
				"<a href=\"http://replay.archive.org/2001/http://www.example.com/foo.html\">foo</a>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	public void testAnchorHrefAbsoluteInJavascript() throws Exception {
		final String input = "<html>" +
				"<a href=\"javascript:doWin('http://www.symphony.org')\">American Symphony Orchestra League</a>" +
				"</html>";
		final String expected = "<html>" +
				"<a href=\"javascript:doWin('http://replay.archive.org/2001/http://www.symphony.org')\">American Symphony Orchestra League</a>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
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
	/**
	 * HTML entities in &lt;STYLE> element are not unescaped.
	 * (although this is an unlikely scenario)
	 * @throws Exception
	 */
	public void testStyleElementBackgroundUrlNoUnescape() throws Exception {
	    final String input = "<html>" +
	    		"<head>" +
	    		"<style type=\"text/css\">" +
	    		"#head{" +
	    		"background-image:url(/genbg?a=2&amp;b=1);" +
	    		"}" +
	    		"</style>" +
	    		"</head>" +
	    		"</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">" +
                "#head{" +
                "background-image:url(http://replay.archive.org/2001im_" +
                "/http://www.example.com/genbg?a=2&amp;b=1);" +
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

    /**
     * URL-rewrite must not unescape HTML entities in URL.
     * <p>Reported in <a href="https://webarchive.jira.com/browse/ARI-3774">ARI-3774</a>.
     * Now all attribute values are unescaped before processing, and then escaped back
     * before writing out.  This has a side-effect: bare "{@code &}" gets rewritten to
     * "{@code &amp;}"</p>
     *
     * @throws Exception
     */
	public void testHTMLEntityInURL() throws Exception {
		// note "&amp;amp" - it should appear in translated URL as it does in the original.
		final String input = "<html>"
				+ "<body>"
				+ "<iframe src=\"https://example.com/player/?url=https%3A//api.example.com/"
				+ "tracks/135768597%3Ftoken%3Dsss&amp;amp;auto_play=false&bare=1\"></iframe>"
				+ "</body>"
				+ "</html>";
		final String expected = "<html>"
				+ "<body>"
				+ "<iframe src=\"http://replay.archive.org/2001if_/https://example.com/player/?url=https%3A//api.example.com/"
				+ "tracks/135768597%3Ftoken%3Dsss&amp;amp;auto_play=false&amp;bare=1\"></iframe>"
				+ "</body>"
				+ "</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * test of {@code unescapeAttributeValue } == {@code false} case.
	 * bare "{@code &}" is unchanged.
	 * @throws Exception
	 */
	public void testUnescapeAttributeValuesFalse() throws Exception {
		// disable unescaping HTML entities in attribute value.
		delegator.setUnescapeAttributeValues(false);

		// note "&amp;amp" - it should appear in translated URL as it does in the original.
		final String input = "<html>"
				+ "<body>"
				+ "<iframe src=\"https://example.com/player/?url=https%3A//api.example.com/"
				+ "tracks/135768597%3Ftoken%3Dsss&amp;amp;auto_play=false&intact=1\"></iframe>"
				+ "</body>"
				+ "</html>";
		final String expected = "<html>"
				+ "<body>"
				+ "<iframe src=\"http://replay.archive.org/2001if_/https://example.com/player/?url=https%3A//api.example.com/"
				+ "tracks/135768597%3Ftoken%3Dsss&amp;amp;auto_play=false&intact=1\"></iframe>"
				+ "</body>"
				+ "</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	public void testLinkElement() throws Exception {
		final String input = "<html>" +
				"<head>" +
				"  <link rel=\"stylesheet\" href=\"basic.css?v=1.0&amp;l=en\">" +
				"  <link rel=\"shortcut icon\" href=\"icon.png?v=1.0&amp;rg=en\">" +
				"</head>" +
				"<body>" +
				"</body>";
		final String expected = "<html>" +
				"<head>" +
				"  <link rel=\"stylesheet\" href=\"http://replay.archive.org/2001cs_/http://www.example.com/basic.css?v=1.0&amp;l=en\">" +
				"  <link rel=\"shortcut icon\" href=\"http://replay.archive.org/2001im_/http://www.example.com/icon.png?v=1.0&amp;rg=en\">" +
				"</head>" +
				"<body>" +
				"</body>";
		assertEquals(expected, doEndToEnd(input));
	}

	public void testStyleAttribute() throws Exception {
		final String input = "<html>" +
				"<body>" +
				"<div style=\"background-image:url(genbg?a=1&amp;b=2);\">" +
				"blah" +
				"</div>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<body>" +
				"<div style=\"background-image:url(http://replay.archive.org/2001im_/http://www.example.com/genbg?a=1&amp;b=2);\">" +
				"blah" +
				"</div>" +
				"</body>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	public String doEndToEnd(String input) throws Exception {
		final String baseUrl = "http://www.example.com/";
		final String timestamp = "2001";
		final String outputCharset = "UTF-8";
		final String charSet = "UTF-8";
		
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(charSet));
		
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
    	while ((node = lex.nextNode()) != null) {
    	    delegator.handleNode(context, node);
    	}
    	delegator.handleParseComplete(context);

		// At this point, baos contains the utf-8 encoded bytes of our result:
		return new String(baos.toByteArray(),outputCharset);
	}

    /**
     * test expected behavior of htmlparser.
     * <p>htmlparser does neither unescape HTML entities found in text, nor
     * escape special characters in Node.toHtml().  We have a workaround based on this
     * behavior.  If this expectation breaks, we need to modify our code.</p>
     * @throws Exception
     */
    public void testHtmlParser() throws Exception {
    	final String html = "<html>" +
    			"<body>" +
    			"<a href=\"http://example.com/api?a=1&amp;b=2&c=3&#34;\">anchor</a>" +
    			"</body>" +
    			"</html>";
    	byte[] bytes = html.getBytes();
    	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    	Page page = new Page(bais, "UTF-8");
    	Lexer lexer = new Lexer(page);
    	Node node;
    	while ((node = lexer.nextNode()) != null) {
    		if (node instanceof Tag) {
    			Tag tag = (Tag)node;
    			if (tag.getTagName().equalsIgnoreCase("A") && !tag.isEndTag()) {
    				assertEquals("href", "http://example.com/api?a=1&amp;b=2&c=3&#34;", tag.getAttribute("HREF"));

    				String htmlout = tag.toHtml();
    				assertEquals("toHtml output", "<a href=\"http://example.com/api?a=1&amp;b=2&c=3&#34;\">", htmlout);
    			}
    		}
    	}
    }
}
