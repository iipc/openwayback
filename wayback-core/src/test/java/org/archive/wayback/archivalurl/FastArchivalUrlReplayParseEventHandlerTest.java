package org.archive.wayback.archivalurl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.replay.html.transformer.JSStringTransformer;
import org.archive.wayback.util.htmllex.ContextAwareLexer;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;

/**
 * test {@link FastArchivalUrlReplayParseEventHandler}.
 * also covers {@link StandardAttributeRewriter}.
 *
 */
public class FastArchivalUrlReplayParseEventHandlerTest extends TestCase {

	FastArchivalUrlReplayParseEventHandler delegator;

	final String baseUrl = "http://www.example.com/";
	final String timestamp = "2001";
	final String outputCharset = "UTF-8";
	final String charSet = "UTF-8";

    ArchivalUrlResultURIConverter uriConverter;
    ArchivalUrlContextResultURIConverterFactory fact;
    ReplayParseContext context;
	JSPExecutor jspExec = null;

	@Override
	protected void setUp() throws Exception {
		uriConverter = new ArchivalUrlResultURIConverter();
		uriConverter.setReplayURIPrefix("http://replay.archive.org/");

		fact = new ArchivalUrlContextResultURIConverterFactory(
			(ArchivalUrlResultURIConverter)uriConverter);

		// The URL of the page, for resolving in-page relative URLs:
		CaptureSearchResult capture = new CaptureSearchResult();
		capture.setCaptureTimestamp(timestamp);
		capture.setOriginalUrl(baseUrl);
		// urlKey is not set as it is unused

		// set up the context:
		context = new ReplayParseContext(fact, capture);
		context.setOutputCharset(outputCharset);

		delegator = new FastArchivalUrlReplayParseEventHandler();
		delegator.setEndJsp(null);
		delegator.setJspInsertPath(null);
		delegator.init();
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
				"<a href=\"foo.html\">foo</a>" +
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
                "@import \"style1.css\";\n" +
                "@import 'style2.css\';\n" +
                "@import 'http://replay.archive.org/2001cs_/http://archive.org/common.css';\n" +
                "}" +
                "</style>" +
                "</head>" +
                "</html>";
        assertEquals(expected, doEndToEnd(input));
	}
	
	public void testStyleElmeentImportUrlInsideCDATA() throws Exception {
        final String input = "<html>" +
                "<head>" +
                "<style type=\"text/css\">/*<![CDATA[*/\n" +
				"    @import \"/shared.css\";\n" +
				"/*]]>*/</style>" +
                "</head>" +
                "</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">/*<![CDATA[*/\n" +
				"    @import \"http://replay.archive.org/2001cs_/http://www.example.com/shared.css\";\n" + 
				"/*]]>*/</style>" +
                "</head>" +
                "</html>";
        String out = doEndToEnd(input);
        assertEquals(expected, out);
	}

	/**
	 * Some people enclose in-line STYLE content with HTML Comment
	 * for better support for stone age browsers.
	 * @throws Exception
	 */
	public void testStyleElmeentImportUrlInsideHTMLComment() throws Exception {
        final String input = "<html>" +
                "<head>" +
                "<style type=\"text/css\">\n" +
				"<!-- @import '/shared.css'; -->\n" +
				"</style>" +
                "</head>" +
                "</html>";
        final String expected = "<html>" +
                "<head>" +
                "<style type=\"text/css\">\n" +
				"<!-- @import 'http://replay.archive.org/2001cs_/http://www.example.com/shared.css'; -->\n" + 
				"</style>" +
                "</head>" +
                "</html>";
        String out = doEndToEnd(input);
        assertEquals(expected, out);
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
		((StandardAttributeRewriter)delegator.getAttributeRewriter()).setUnescapeAttributeValues(false);

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
				"  <link rel=\"stylesheet\" href=\"basic.css?v=1.0&amp;l=en\">" +
				"  <link rel=\"shortcut icon\" href=\"icon.png?v=1.0&amp;rg=en\">" +
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
				"<div style=\"background-image:url(genbg?a=1&amp;b=2);\">" +
				"blah" +
				"</div>" +
				"</body>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * test of rewriting SCRIPT tag.
	 * Also covered is a feature for disabling script by returning {@code null} from
	 * {@code jsBlockTrans} (This feature may be removed/redesigned at any time).
	 * @throws Exception
	 */
	public void testDisableScriptElement() throws Exception {
		delegator.setJsBlockTrans(new StringTransformer() {
			@Override
			public String transform(ReplayParseContext context, String input) {
				if (input.equals("dropthis.js"))
					return null;
				else
					return input;
			}
		});
		final String input = "<html>" +
				"<head>" +
				"<script src=\"rewrite.js\"></script>" +
				"<script src=\"dropthis.js\"></script>" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"<script src=\"rewrite.js\"></script>" +
				"<script src=\"\"></script>" +
				"</head>" +
				"<body>" +
				"</body>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * URL rewrite takes {@code BASE} element into account.
	 * @throws Exception
	 */
	public void testBase() throws Exception {
		final String input = "<html>" +
				"<base href='http://othersite.com/'>" +
				"<body>" +
				"<a href='nextpage.html'>next page</a>" +
				"<base href='http://anothersite.com/'>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<base href='http://replay.archive.org/2001/http://othersite.com/'>" +
				"<body>" +
				"<a href='nextpage.html'>next page</a>" +
				"<base href='http://replay.archive.org/2001/http://anothersite.com/'>" +
				"</body>" +
				"</html>";
		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * test of additional attribute rewrite rules for {@link StandardAttributeRewriter}.
	 * additional rules takes precedence over default one, if they are of the same specificity.
	 * @throws Exception
	 */
	public void testAdditionalAttributeRewriteRules() throws Exception {
		// adding custom rewrite rules through backdoor...
		Properties rules = new Properties();
		rules.setProperty("SPAN.DATA-URI.type", "an");
		rules.setProperty("A[ROLE=logo.download].HREF.type", "im");
		rules.setProperty("LINK[TYPE=text/javascript].HREF.type", "js");
		((StandardAttributeRewriter)delegator.getAttributeRewriter()).loadRulesFromProperties(rules);

		final String input = "<html>" +
				"<head>" +
				"<link rel='stylesheet' type='text/javascript' href='styles.js'>" +
				"</head>" +
				"<body>" +
				"<span data-uri='http://datasource.example.com/data1'></span>" +
				"<a href='logo.png' role='logo.download'>download logo</a>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"<link rel='stylesheet' type='text/javascript' href='styles.js'>" +
				"</head>" +
				"<body>" +
				"<span data-uri='http://replay.archive.org/2001/http://datasource.example.com/data1'></span>" +
				"<a href='logo.png' role='logo.download'>download logo</a>" +
				"</body>" +
				"</html>";

		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * test of {@link StandardAttributeRewriter#setDefaultRulesDisabled(boolean)}
	 * @throws Exception
	 */
	public void testDisableDefaultRules() throws Exception {
		// use local instance for modifying defaultRulesDisabled property.
		StandardAttributeRewriter rewriter = new StandardAttributeRewriter();
		rewriter.setDefaultRulesDisabled(true);
		Properties rules = new Properties();
		rules.setProperty("A[REWRITE=TRUE].HREF.type", "an");
		rewriter.setConfigProperties(rules);
		rewriter.init();
		delegator.setAttributeRewriter(rewriter);

		final String input ="<html>" +
				"<body>" +
				"<a href=\"ignore.html\">ignore this</a>" +
				"<a href=\"rewrite.html\" rewrite=\"true\">rewrite this</a>" +
				"</body>" +
				"</html>";
		final String expected ="<html>" +
				"<body>" +
				"<a href=\"ignore.html\">ignore this</a>" +
				"<a href=\"rewrite.html\" rewrite=\"true\">rewrite this</a>" +
				"</body>" +
				"</html>";

		assertEquals(expected, doEndToEnd(input));
	}

	/**
	 * JSPExecutor patched to return predictable text without
	 * actually running JSP.
	 */
	protected static class TestJSPExecutor extends JSPExecutor {
		public TestJSPExecutor() {
			// we cannot pass null WaybackRequest to JSPExecutor constructor,
			// because it accesses WaybackRequst.isAjaxRequest() method.
			//super(null, null, null, stubWaybackRequest(), null, null, null);
			super(null, null, new UIResults(stubWaybackRequest(), null));
		}
		// for testing with context flags (ex. fw_)
		public TestJSPExecutor(WaybackRequest wbRequest) {
			//super(null, null, null, wbRequest, null, null, null);
			super(null, null, new UIResults(wbRequest, null));
		}
		@Override
		public String jspToString(String jspPath) throws ServletException,
				IOException {
			return "[[[JSP-INSERT:" + jspPath + "]]]";
		}
		private static WaybackRequest stubWaybackRequest() {
			WaybackRequest wbRequest = new WaybackRequest();
			// make sure ajaxRequest is false (true disables JSP inserts)
			// it's false by default currently, but just in case (paranoia).
			wbRequest.setAjaxRequest(false);
			return wbRequest;
		}
	}

	/**
	 * Servers often return non-HTML resource with {@code Content-Type: text/html}.
	 * Inserting HTML annotation to non-HTML resource can break its replay.
	 * Therefore, don't insert {@code EndJsp} if resource does not appear to be
	 * HTML.
	 * @throws Exception
	 */
	public void testNoEndJspForNonHTML() throws Exception {
		delegator.setEndJsp("end.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "{\"a\": 1}";
		final String expected = "{\"a\": 1}";

		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}
	
	/**
	 * JSP inserts: well formed case.
	 * @throws Exception
	 */
	public void testInserts() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" +
				"</head>" +
				"<body>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">" +
				"</head>" +
				"<body>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);		
	}

	/**
	 * Pathological case:
	 * Missing HEAD tag. head-insert shall be inserted just before
	 * the first open tag (excluding !DOCTYPE and HTML).
	 * @throws Exception
	 */
	public void testMissingHeadTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<title>BarBar</title>" +
				"<body>" +
				"Content" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<body>" +
				"[[[JSP-INSERT:body.jsp]]]" +
				"Content" +
				"</body>" +
				"</html>";

		String output = doEndToEnd(input);
		assertEquals(expected, output);
	}

	public void testMissingBodyTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"</head>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"</head>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Pathological case:
	 * Content has {@code <HEAD>} but missing {@code </HEAD>} and {@code BODY}. 
	 * @throws Exception
	 */
	public void testMissingHeadCloseTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<body>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"</head>" +
				"<body>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Pathological case:
	 * Content has {@code <HEAD>} but missing {@code </HEAD>} and {@code BODY}.
	 * @throws Exception
	 */
	public void testMissingHeadCloseAndBodyTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Pathological case:
	 * both HEAD and BODY tags are missing. There are some in-HEAD tags.
	 * @throws Exception
	 */
	public void testMissingHeadAndBodyTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input =
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>";
		final String expected =
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Pathological case:
	 * both HEAD and BODY tags are missing. There are some in-HEAD tags.
	 * @throws Exception
	 */
	public void testMissingHeadAndBodyTagStartsWithContentTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input =
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>";
		final String expected =
				"[[[JSP-INSERT:head.jsp]]]" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * Pathological case:
	 * Content-producing tag appearing before BODY tag.
	 * body-insert is inserted before the tag, not after the BODY tag.
	 * @throws Exception
	 */
	public void testNonHeadTagBeforeBodyTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html>" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"<div>TEXT</div>" +
				"<body>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"<script src=\"a.js\"></script>" +
				"[[[JSP-INSERT:body-insert.jsp]]]" +
				"<div>TEXT</div>" +
				"<body>" +
				"<p align=\"center\">" +
				"<img src=\"map.gif\">" +
				"</p>" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * pathological case of two HEAD tags.
	 * head-insert shall be inserted after the first HEAD tag.
	 * @throws Exception
	 */
	public void testExtraHeadTag() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		jspExec = new TestJSPExecutor();
		
		final String input = "<html>" +
				"<head>" +
				"<head>" +
				"<title>BarBar</title>" +
				"</head>" +
				"Content" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<head>" +
				"<title>BarBar</title>" +
				"</head>" +
				"Content" +
				"</html>";
		
		String output = doEndToEnd(input);
		assertEquals(expected, output);
	}

	/**
	 * body-inset shall not be inserted to resource rendered
	 * insider {@code FRAME} (identified by {@code fw_} context
	 * flag, {@code frameWrapperContext} in {@code WaybackRequest}.
	 * @throws Exception
	 */
	public void testNoBodyInsertForFrameContent() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAjaxRequest(false);
		wbRequest.setFrameWrapperContext(true);

		jspExec = new TestJSPExecutor(wbRequest);

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"</head>" +
				"<body>" +
				"content" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"</head>" +
				"<body>" +
				"content" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * similarly for {@code IFRAME}.
	 * @throws Exception
	 */
	public void testNoBodyInsertForIFrameContent() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setAjaxRequest(false);
		wbRequest.setIFrameWrapperContext(true);

		jspExec = new TestJSPExecutor(wbRequest);

		final String input = "<html>" +
				"<head>" +
				"<title>BarBar</title>" +
				"</head>" +
				"<body>" +
				"content" +
				"</body>" +
				"</html>";
		final String expected = "<html>" +
				"<head>" +
				"[[[JSP-INSERT:head.jsp]]]" +
				"<title>BarBar</title>" +
				"</head>" +
				"<body>" +
				"content" +
				"</body>" +
				"</html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

    /**
	 * Pathological case:
	 * content-bearing tags before FRAMESET, even before HEAD.
	 * currently {@link FastArchivalUrlReplayParseEventHandler} alone cannot
	 * handle this case well. {@link ArchivalUrlSAXRewriteReplayRenderer} has
	 * workaround for this kind of case.
	 * @throws Exception
	 */
	public void testFramesetWithExtraTags() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input =
				"<span><p>" +
				"<head>" +
				"</head>" +
				"<frameset border=\"0\" cols=\"150,*\" frameborder=\"0\">" +
				"<frame>" +
				"<frame>" +
				"</frameset>" +
				"</span>";
		final String expected =
				"[[[JSP-INSERT:head.jsp]]][[[JSP-INSERT:body-insert.jsp]]]" +
				"<span><p>" +
				"<head>" +
				"</head>" +
				"<frameset border=\"0\" cols=\"150,*\" frameborder=\"0\">" +
				"<frame>" +
				"<frame>" +
				"</frameset>" +
				"</span>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

	/**
	 * HTMLParser appears to parse the content of {@code <SCRIPT>} element
	 * as HTML.
	 * @throws Exception
	 */
	public void testElementLookAlikeInScript() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<html><head><script>/</g;900>a;a<k;</script></head>" +
				"<body></body></html>";
		final String expected = "<html><head>[[[JSP-INSERT:head.jsp]]]<script>/</g;900>a;a<k;</script></head>" +
				"<body>[[[JSP-INSERT:body-insert.jsp]]]</body></html>";

        String out = doEndToEnd(input);
        System.out.println(out);

        assertEquals(expected, out);
	}

	public void testXHTML() throws Exception {
		delegator.setHeadInsertJsp("head.jsp");
		delegator.setJspInsertPath("body-insert.jsp");
		delegator.setEndJsp("end.jsp");
		jspExec = new TestJSPExecutor();

		final String input = "<!-- comment -->" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<html xml:lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"  <head>" +
				"    <title>homepage</title>" +
				"  </head>" +
				"  <body id=\"home\">" +
				"  body body" +
				"  </body>" +
				"</html>";
		final String expected = "<!-- comment -->" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<html xml:lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">" +
				"  <head>[[[JSP-INSERT:head.jsp]]]" +
				"    <title>homepage</title>" +
				"  </head>" +
				"  <body id=\"home\">[[[JSP-INSERT:body-insert.jsp]]]" +
				"  body body" +
				"  </body>" +
				"</html>[[[JSP-INSERT:end.jsp]]]";
		String out = doEndToEnd(input);
		System.out.println(out);
		assertEquals(expected, out);
	}

	/**
	 * test of rewriting URLs in DOM event handler attributes.
	 * <p>only limited set of attributes are supported currently.</p>
	 * @throws Exception
	 */
	public void testDOMEventHandlers() throws Exception {
		// Need a local test instance because changing jsBlockTrans after calling init()
		// has no effect currently.
		delegator = new FastArchivalUrlReplayParseEventHandler();
		delegator.setEndJsp(null);
		delegator.setJspInsertPath(null);
		// sample input below has two backslashes - use custom jsBlockTrans
		JSStringTransformer jsBlockTrans = new JSStringTransformer();
		jsBlockTrans.setRegex("['\"](https?:\\\\{0,2}/\\\\{0,2}/[a-zA-Z0-9_@.-]+)");
		delegator.setJsBlockTrans(jsBlockTrans);
		delegator.init();
		
		// test sample from Facebook timeline
		final String input = "<html><body>" +
				"<a class=\"_1xw shareLink _1y0\" href=\"http://www.facebook.com/l.php?" +
				"u=http%3A%2F%2Fwww.ncadv.org%2F&amp;h=PAQH&amp;" +
				"enc=AZPB&amp;" +
				"s=1\" target=\"_blank\" rel=\"nofollow\" " +
				"onmouseover=\"LinkshimAsyncLink.swap(this, &quot;http:\\\\/\\\\/www.ncadv.org\\\\/&quot;);\" " +
				"onclick=\"LinkshimAsyncLink.swap(this, &quot;http:\\\\/\\\\/www.facebook.com\\\\/l.php?" +
				"u=http\\\\u00253A\\\\u00252F\\\\u00252Fwww.ncadv.org\\\\u00252F&amp;h=PAQH&amp;enc=AZPB&amp;s=1&quot;);\">" +
				"</body></html>";
		// Note: onmouseover attribute is NOT rewritten currently.
		// Note: URL is first normalized by ParseContext.resolve(), in which backslash escapes are
		//   removed. so "http:\\/\\/www.facebook.com" becomes "http://www.facebook.com"
		final String expected = "<html><body>" +
				"<a class=\"_1xw shareLink _1y0\" href=\"http://replay.archive.org/2001/http://www.facebook.com/l.php?" +
				"u=http%3A%2F%2Fwww.ncadv.org%2F&amp;h=PAQH&amp;" +
				"enc=AZPB&amp;" +
				"s=1\" target=\"_blank\" rel=\"nofollow\" " +
				"onmouseover=\"LinkshimAsyncLink.swap(this, &quot;http:\\\\/\\\\/www.ncadv.org\\\\/&quot;);\" " +
				"onclick=\"LinkshimAsyncLink.swap(this, &quot;http://replay.archive.org/2001/http://www.facebook.com\\\\/l.php?" +
				"u=http\\\\u00253A\\\\u00252F\\\\u00252Fwww.ncadv.org\\\\u00252F&amp;h=PAQH&amp;enc=AZPB&amp;s=1&quot;);\">" +
				"</body></html>";
		String output = doEndToEnd(input);
		System.out.println(output);
		assertEquals(expected, output);
	}

    public String doEndToEnd(String input) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(charSet));
		
		// To make sure we get the length, we have to buffer it all up...
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		context.setOutputStream(baos);
		context.setJspExec(jspExec);

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
		return new String(baos.toByteArray(), outputCharset);
	}

	// The followings checks expected (quirky) behaviors of HTMLParser.
	// If these expectations get broken by HTMLParser upgrades, it is very likely
	// we need to change our code.

    /**
     * test expected behavior of htmlparser.
     * <p>htmlparser does neither unescape HTML entities found in text, nor
     * escape special characters in Node.toHtml().  We have a workaround based on this
     * behavior.  If this expectation breaks, we need to modify our code.</p>
     * @throws Exception
     */
    public void testHtmlParser_attributeValueEscaping() throws Exception {
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

    /**
     * test expected behavior of {@code HTMLParser} - handling of CDATA.
     * {@code HTMLParser} gives out {@code <![CDATA[ ... ]]>} as single
     * {@code TagNode}.  Quirk is that {@code tagName} can also include
     * characters following "<![CDATA[". And apparently there's no way
     * to update the content.
     * @throws Exception
     */
    public void testHtmlParser_CDATA() throws Exception {
    	final String html = "<![CDATA[aaaa\nbbbb]]>";

    	byte[] bytes = html.getBytes();
    	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    	Page page = new Page(bais, "UTF-8");
    	Lexer lexer = new Lexer(page);
    	Node node;

    	node = lexer.nextNode();
    	// HTMLParser returns CDATA section as TagNode
    	assertTrue(node instanceof TagNode);
    	TagNode tag = (TagNode)node;
    	// whose tagName is "![CDATA[" *plus* non-whitespace chars following
    	// it.  And if they are alphabets, they get capitalized.
    	assertTrue(tag.getTagName().startsWith("![CDATA["));
    	assertEquals("AAAA", tag.getTagName().substring("![CDATA[".length()));
    	// Fortunately, getText() returns entire CDATA section, including
    	// leading "!CDATA[" and trailing "]]" (no < and >), unmodified.
    	String text = tag.getText();
    	System.out.println("text=\"" + text + "\" (" + text.length() + " chars)");
    	assertEquals("![CDATA[aaaa\nbbbb]]", text);
    	// but setText() results in ClassCastException
    	try {
    		tag.setText("![CDATA[bbbb]]");
    		fail("setText() did not throw an Exception");
    	} catch (ClassCastException ex) {
    		// expected
    	}
    }
}
