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
package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.replay.html.rewrite.DisableJSIncludeRewriteRule;
import org.archive.wayback.replay.html.transformer.BlockCSSStringTransformer;
import org.archive.wayback.replay.html.transformer.JSStringTransformer;
import org.archive.wayback.replay.html.transformer.URLStringTransformer;
import org.archive.wayback.util.htmllex.NodeUtils;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.ParseEventHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

/**
 * Lean and mean ParseEventHandler implementing current best-known server-side
 * HTML rewrite rules, and should be much faster than the fully configurable
 * version.
 * 
 * <p>This class has kludgy support for disabling JavaScript inclusion
 * with <code>&lt;SCRIPT SRC=...&gt;</code> element.  <code>jsBlockTrans</code>,
 * whose primary use is translating JavaScript code block (inline &lt;SCRIPT&gt;,
 * <code>javascript:</code> URI in <code>HREF</code> attribute, and event
 * handler attributes), is also called with a value of <code>SRC</code> attribute
 * of &lt;SCRIPT&gt; tag.  If <code>jsBlockTrans</code> returns either <code>null</code>
 * or an empty String, &lt;SCRIPT&gt; element is disabled by changing <code>SRC</code> to
 * an empty value.  Note that in this case <code>jsBlockTrans</code> is used only for
 * a test, and its return value is simply discarded.
 * URL translation is done by a subsequent call to {@link URLStringTransformer} for
 * <code>js_</code> context.  This feature is very likely a subject of refactoring
 * in the future.</p>
 *
 * @author brad
 * 
 */
public class FastArchivalUrlReplayParseEventHandler implements
		ParseEventHandler {

	private static final Logger LOGGER = Logger
		.getLogger(FastArchivalUrlReplayParseEventHandler.class.getName());

	public final static String FERRET_DONE_KEY = FastArchivalUrlReplayParseEventHandler.class
		.toString();

	protected final static String FERRET_IN_HEAD = "FERRET_IN_HEAD";

	private String jspInsertPath = "/WEB-INF/replay/DisclaimChooser.jsp";
	private String endJsp = "/WEB-INF/replay/ArchiveComment.jsp";
	private String startJsp = null;

	private final String[] okHeadTags = { "![CDATA[*", "![CDATA[", "?",
		"!DOCTYPE", "HTML", "HEAD", "BASE", "LINK", "META", "TITLE", "STYLE",
		"SCRIPT", "BGSOUND" };
	private HashMap<String, Object> okHeadTagMap = null;
	private final static String FRAMESET_TAG = "FRAMESET";
	private final static String BODY_TAG = "BODY";

	protected static final String FERRET_HEAD_INSERTED = "FERRET_HEAD_INSERTED";

	private BlockCSSStringTransformer cssBlockTrans = new BlockCSSStringTransformer();
	private StringTransformer jsBlockTrans = new JSStringTransformer();

	protected String headInsertJsp = null;

	// @see #transformAttrWhere
	private boolean unescapeAttributeValues = true;

	private AttributeRewriter attributeRewriter;

	public void init() throws IOException {
		if (attributeRewriter == null) {
			StandardAttributeRewriter b = new StandardAttributeRewriter();
			if (jsBlockTrans != null)
				b.setJsBlockTrans(jsBlockTrans);
			b.setUnescapeAttributeValues(unescapeAttributeValues);
			b.init();
			attributeRewriter = b;
		}
	}

	/** Constructor... */
	public FastArchivalUrlReplayParseEventHandler() {
		okHeadTagMap = new HashMap<String, Object>(okHeadTags.length);
		for (String tag : okHeadTags) {
			okHeadTagMap.put(tag, null);
		}
	}
	
	// TODO: This should all be refactored up into an abstract base class with
	// default no-op methods, allowing a subclass to only override the ones they
	// want...
	public void handleNode(ParseContext pContext, Node node) throws IOException {
		ReplayParseContext context = (ReplayParseContext)pContext;
		if (NodeUtils.isRemarkNode(node)) {
			RemarkNode remarkNode = (RemarkNode)node;
			remarkNode.setText(jsBlockTrans.transform(context,
				remarkNode.getText()));
			emit(context, null, node, null);

		} else if (NodeUtils.isTextNode(node)) {
			TextNode textNode = (TextNode)node;
			if (context.isInCSS()) {
				handleCSSTextNode(context, textNode);
			} else if (context.isInScriptText()) {
				handleJSTextNode(context, textNode);
			}
			emit(context, null, textNode, null);
//				handleContentTextNode(context,textNode);
		} else if (NodeUtils.isTagNode(node)) {
			TagNode tagNode = (TagNode)node;

			if (tagNode.isEndTag()) {
				if (tagNode.getTagName().equals("HEAD")) {
					context.putData(FERRET_IN_HEAD, null);
				}

				if (checkAllowTag(pContext, tagNode)) {
					emit(context, null, tagNode, null);
				}

//				handleCloseTagNode(context,tagNode);
			} else if (tagNode.getTagName().startsWith("![CDATA[")) {
				// CDATA section is delivered as TagNode, and it
				// appears there's no ordinary way of replacing its
				// body content. Also CSS/JS handling method wants
				// TextNode. Create a temporary TextNode for them,
				// and write "<![CDATA["..."]]>" around it.
				String text = tagNode.getText();
				int s = "![CDATA[".length();
				// text is supposed to end with "]]", but just in case.
				int e = text.endsWith("]]") ? text.length() - 2 : text.length();
				if (context.isInCSS()) {
					TextNode textNode = new TextNode(text.substring(s, e));
					handleCSSTextNode(context, textNode);
					emit(context, "<![CDATA[", textNode, "]]>");
				} else if (context.isInScriptText()) {
					TextNode textNode = new TextNode(text.substring(s, e));
					handleJSTextNode(context, textNode);
					emit(context, "<![CDATA[", textNode, "]]>");
				} else {
					emit(context, null, tagNode, null);
				}
			} else {
				context.setInHTML(true);
				// assume start, possibly empty:
				handleOpenTagNode(context, tagNode);
			}
		} else {
			throw new IllegalArgumentException("Unknown node type..");
		}
	}

	/**
	 * @param context
	 * @param textNode
	 * @throws IOException 
	 */
	private void handleCSSTextNode(ReplayParseContext context, TextNode textNode) throws IOException {
		textNode.setText(cssBlockTrans.transform(context, textNode.getText()));
		//emit(context, null, textNode, null);
	}
	/**
	 * @param context
	 * @param textNode
	 * @throws IOException 
	 */
	private void handleJSTextNode(ReplayParseContext context, TextNode textNode) throws IOException {
		boolean alreadyInsertedHead = (context.getData(FERRET_HEAD_INSERTED) != null);
		context.incJSBlockCount();
		if (alreadyInsertedHead) {
			textNode.setText(jsBlockTrans.transform(context, textNode.getText()));
		}
		//emit(context, null, textNode, null);
	}
	
	/**
	 * kludgy support for selectively disabling JavaScript that messes up
	 * replay.
	 * <p>
	 * If {@code jsBlockTrans.transform} returns {@code null} or empty for
	 * {@code SCRIPT/@SRC}, {@code SCRIPT} element is disabled by replacing
	 * {@code SRC} attribute with empty value. {@link DisableJSIncludeRewriteRule}
	 * provides one implementation to make use of this behavior.
	 * </p>
	 * @param context {@link ReplayParseContext}
	 * @param tagNode {@code SCRIPT} tag.
	 * @see DisableJSIncludeRewriteRule
	 */
	private void handleJSIncludeNode(ReplayParseContext context, TagNode tagNode) {
		String file = tagNode.getAttribute("SRC");
		if (file != null) {
			String result = jsBlockTrans.transform(context, file);
			// URL rewrite is done by AttributeRewriter, which should ignore
			// empty value.
			if (result == null || result.isEmpty()) {
				tagNode.setAttribute("SRC", "");
			}
		}
	}

	private void handleOpenTagNode(ReplayParseContext context, TagNode tagNode) 
	throws IOException {
		
		String preEmit = null;
		String postEmit = null;

		String tagName = tagNode.getTagName();
		
		boolean alreadyInsertedHead = (context.getData(FERRET_HEAD_INSERTED) != null);
		boolean insertedJsp = context.getData(FERRET_DONE_KEY) != null;
		boolean inHead = (context.getData(FERRET_IN_HEAD) != null);

		if (!alreadyInsertedHead) {
			// If we're at the beginning of a <head> tag, and haven't inserted yet, 
			// insert right AFTER head tag
			if (tagName.equals("HEAD")) {
				emitHeadInsert(context, tagNode, true);
				context.putData(FERRET_IN_HEAD, FERRET_IN_HEAD);
				// this means HEAD tag does not get its attribute
				// rewritten. probably that's ok...
				return;
			}
			// If we're at the beginning of any tag, other than <html>,
			// <!DOCTYPE ...>, <?xml ... ?> (tagName is "?" for it),
			// (including <body>) and haven't inserted yet,
			// insert right BEFORE the next tag, also continue other default processing
			// of the tag
			if (!tagName.equals("HTML") && !tagName.equals("!DOCTYPE") &&
					!tagName.equals("?")) {
				emitHeadInsert(context, null, false);
				// Don't return continue to further processing
			}
		} else if (tagName.equals(BODY_TAG) && inHead) {
			context.putData(FERRET_IN_HEAD, null);
			inHead = false;

			OutputStream out = context.getOutputStream();
			out.write("</head>".getBytes(context.getOutputCharset()));
		}
				
		// Time to insert the JSP header?
		//IK added check to avoid inserting inside css or script
		if (!insertedJsp && !context.isInCSS() && !context.isInScriptText()) {
			if (tagName.equals(FRAMESET_TAG)) {
				// don't put the insert in FRAMESET
				context.putData(FERRET_DONE_KEY, "");
			} else if (tagName.equals(BODY_TAG)) {
				postEmit = bodyInsertContent(context);
				context.putData(FERRET_DONE_KEY, "");
			} else if (!okHeadTagMap.containsKey(tagName)) {
				// hrm... we are seeing a node that should be in
				// the body.. lets emit the jsp now, *before*
				// the current Tag:
				preEmit = bodyInsertContent(context);
				context.putData(FERRET_DONE_KEY, "");
			}
		}
		
		if (tagName.equals("BASE")) {
			String baseURL = tagNode.getAttribute("HREF");
			if (baseURL != null) {
				context.setBaseUrl(baseURL);
			}
		} else if (tagName.equals("SCRIPT")) {
			// hacky disable-SCRIPT feature.
			handleJSIncludeNode(context, tagNode);
		}

		// now do all the usual attribute rewriting
		attributeRewriter.rewrite(context, tagNode);

		// drop tags named by rewrite policy as such.
		if (!checkAllowTag(context, tagNode)) return;

		emit(context, preEmit, tagNode, postEmit);

	}
	
	protected boolean checkAllowTag(ParseContext context, TagNode tagNode) {
		String tagName = tagNode.getTagName();
		
		// Check the NOSCRIPT tag, if force-noscript is set,
		// then  skip the NOSCRIPT tags and include contents explicitly
		if (tagName.equals("NOSCRIPT")) {
			String allPolicies = context.getOraclePolicy();
			
			if ((allPolicies != null) && allPolicies.contains("force-noscript")) {
				return false;
			}
		}
		
		return true;
	}
	
	protected void emit(ReplayParseContext context, String pre, Node node, 
			String post) throws IOException {
		
		OutputStream out = context.getOutputStream();
		if (out != null) {
//			Charset charset = Charset.forName(context.getOutputCharset());
			String charset = context.getOutputCharset();

			if (pre != null) {
				out.write(pre.getBytes(charset));
			}
			
			if (node != null) {
				out.write(node.toHtml(true).getBytes(charset));
			}

			if (post != null) {
				out.write(post.getBytes(charset));
			}
		}
	}
	
	public void handleParseComplete(ParseContext pContext) throws IOException {
		// if no HTML element was found (inHTML==false), don't insert EndJsp.
		if (endJsp != null && pContext.isInHTML()) {
			ReplayParseContext context = (ReplayParseContext) pContext;
			OutputStream out = context.getOutputStream();
			String tmp = null; 
			try {
				tmp = context.getJspExec().jspToString(endJsp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			if (tmp != null) {
//				Charset charset = Charset.forName(context.getOutputCharset());
				String charset = context.getOutputCharset();
				out.write(tmp.getBytes(charset));
			}
		}
	}

	public void handleParseStart(ParseContext pContext) throws IOException {
		
		ReplayParseContext context = (ReplayParseContext) pContext;
		
		// Now done in ArchivalUrlSAXRewriteReplayRenderer
//		String policy = context.getJspExec().getUiResults().getResult().getOraclePolicy();
//		
//		if (policy != null) {
//			context.setOraclePolicy(policy);
//		}
		
		if (startJsp != null) {
			OutputStream out = context.getOutputStream();
			String tmp = null; 
			try {
				tmp = context.getJspExec().jspToString(startJsp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			if (tmp != null) {
//				Charset charset = Charset.forName(context.getOutputCharset());
				String charset = context.getOutputCharset();
				out.write(tmp.getBytes(charset));
			}
		}
	}

	/**
	 * set {@link AttributeRewriter} for rewriting attribute values.
	 * if not set, {@link StandardAttributeRewriter} will be used as default.
	 * @param attributeRewriter {@link AttributeRewriter} instance.
	 */
	public void setAttributeRewriter(AttributeRewriter attributeRewriter) {
		this.attributeRewriter = attributeRewriter;
	}

	public AttributeRewriter getAttributeRewriter() {
		return attributeRewriter;
	}

	/**
	 * @return the jspInsertPath
	 */
	public String getJspInsertPath() {
		return jspInsertPath;
	}

	/**
	 * @param jspInsertPath the jspInsertPath to set
	 */
	public void setJspInsertPath(String jspInsertPath) {
		this.jspInsertPath = jspInsertPath;
	}

	/**
	 * @return the commentJsp
	 * @deprecated use getEndJsp()
	 */
	public String getCommentJsp() {
		return getEndJsp();
	}

	/**
	 * @param commentJsp the commentJsp to set
	 * @deprecated use setEndJsp()
	 */
	public void setCommentJsp(String commentJsp) {
		setEndJsp(commentJsp);
	}
	/**
	 * @return the path to the JSP to execute and include at the start of the
	 * document
	 */
	public String getStartsp() {
		return startJsp;
	}

	/**
	 * @param endJsp the path to the JSP to execute and include at the start
	 * of the document
	 */
	public void setStartJsp(String startJsp) {
		this.startJsp = startJsp;
	}
	/**
	 * @return the path to the JSP to execute and include at the end of the
	 * document
	 */
	public String getEndJsp() {
		return endJsp;
	}

	/**
	 * @param endJsp the path to the JSP to execute and include at the end
	 * of the document
	 */
	public void setEndJsp(String endJsp) {
		this.endJsp = endJsp;
	}

	/**
	 * @return the jsBlockTrans
	 */
	public StringTransformer getJsBlockTrans() {
		return jsBlockTrans;
	}

	/**
	 * StringTransformer used for rewriting JavaScript code block
	 * (<code>&lt;SCRIPT&gt;</code> and <code>javascript:</code> attribute).
	 * Also used (abused) as a test whether particular <code>&lt;SCRIPT SRC=...&gt;</code>
	 * should be disabled (See class-level javadoc for details). 
	 * @param jsBlockTrans the jsBlockTrans to set
	 */
	public void setJsBlockTrans(StringTransformer jsBlockTrans) {
		this.jsBlockTrans = jsBlockTrans;
	}

	public String getHeadInsertJsp() {
		return headInsertJsp;
	}

	/**
	 * servlet whose output will be
	 * inserted right after {@code HEAD} tag.
	 * @param headInsertJsp context-relative path
	 */
	public void setHeadInsertJsp(String headInsertJsp) {
		this.headInsertJsp = headInsertJsp;
	}

	protected void emitHeadInsert(ReplayParseContext context, Node node,
			boolean postInsert) throws IOException {
		String headInsert = null;

		if (headInsertJsp == null) {
			this.emit(context, null, node, null);
			return;
		}

		try {
			headInsert = context.getJspExec().jspToString(headInsertJsp);
			context.putData(FERRET_HEAD_INSERTED, FERRET_HEAD_INSERTED);
		} catch (ServletException e) {
			e.printStackTrace();
		}

		if (postInsert) {
			this.emit(context, null, node, headInsert);
		} else {
			this.emit(context, headInsert, node, null);
		}
	}

	/**
	 * return body-insert text.
	 * <p>Run {@code jspInsertPath} and return its output as String.
	 * if {@code jspInsertPath} is {@code null}, or body-insert should not be
	 * inserted into the resource being processed, returns {@code null}.</p>
	 * @param context context for the resource being processed
	 * @return insert text as String, or {@code null} if no insertion shall be
	 *         made.
	 */
	protected String bodyInsertContent(ReplayParseContext context) {
		if (jspInsertPath == null)
			return null;
		JSPExecutor jspExec = context.getJspExec();
		// FIXME bad chain of references. add method to ReplayParseContext?
		WaybackRequest wbRequest = jspExec.getUiResults().getWbRequest();
		// isAnyEmbeddedContext() used as shorthand for (isFrameWrapperContext()
		// && isIFrameWrapperContext()).
		if (wbRequest.isAnyEmbeddedContext())
			return null;
		try {
			return jspExec.jspToString(jspInsertPath);
		} catch (ServletException ex) {
			LOGGER.log(Level.WARNING, "execution of " + jspInsertPath +
					" failed", ex);
			return null;
		} catch (IOException ex) {
			LOGGER.log(Level.WARNING, "erorr executing " + jspInsertPath, ex);
			return null;
		}
	}

	/**
	 *
	 * @return {@code true} if attribute value unescape/re-escape
	 * is enabled.
	 * @deprecated 1.8.1/05-23-2014 moved to {@link StandardAttributeRewriter}.
	 */
	public boolean isUnescapeAttributeValues() {
		return unescapeAttributeValues;
	}

	/**
	 * set this property false if you want to disable unescaping
	 * (and corresponding re-escaping) of attribute values.
	 * <p>By default, HTML entities (such as <code>&amp;amp;</code>)
	 * in attribute values are unescaped before translation attempt,
	 * and then escaped back before writing out.  Although this is
	 * supposedly the right thing to do, it has a side-effect: all
	 * bare "<code>&amp;</code>" (not escaped as "<code>&amp;amp;</code>")
	 * will be replaced by "<code>&amp;amp;</code>".  Setting this property
	 * to <code>false</code> disables it.</p>
	 * <p>As URL rewrite does neither parse nor modify query part, it
	 * should mostly work without unescaping.  But there may be some
	 * corner cases where escaping is crucial.  Don't set this to {@code false}
	 * unless it's absolutely necessary.</p>
	 * @param unescapeAttributeValues <code>false</code> to disable unescaping
	 * @deprecated 1.8.1/05-23-2014 property moved to {@link StandardAttributeRewriter}
	 *   This property still works, but only with {@code StandardAttributeRewriter}.
	 */
	public void setUnescapeAttributeValues(boolean unescapeAttributeValues) {
		this.unescapeAttributeValues = unescapeAttributeValues;
		if (attributeRewriter instanceof StandardAttributeRewriter) {
			((StandardAttributeRewriter)attributeRewriter).setUnescapeAttributeValues(unescapeAttributeValues);
		}
	}
}
