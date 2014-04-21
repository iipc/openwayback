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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.replay.html.transformer.BlockCSSStringTransformer;
import org.archive.wayback.replay.html.transformer.InlineCSSStringTransformer;
import org.archive.wayback.replay.html.transformer.JSStringTransformer;
import org.archive.wayback.replay.html.transformer.MetaRefreshUrlStringTransformer;
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

	public final static String FERRET_DONE_KEY = 
		FastArchivalUrlReplayParseEventHandler.class.toString();
	
	protected final static String FERRET_IN_HEAD = "FERRET_IN_HEAD";

	private String jspInsertPath = "/WEB-INF/replay/DisclaimChooser.jsp";
	private String endJsp = "/WEB-INF/replay/ArchiveComment.jsp";
	private String startJsp = null;

	private final String[] okHeadTags = { "![CDATA[*", "![CDATA[", "?", 
			"!DOCTYPE", "HTML",	"HEAD", "BASE", "LINK", "META", "TITLE", 
			"STYLE", "SCRIPT" , "BGSOUND"};
	private HashMap<String, Object> okHeadTagMap = null;
	private final static String FRAMESET_TAG = "FRAMESET";
	private final static String BODY_TAG = "BODY";

	protected static final String FERRET_HEAD_INSERTED = "FERRET_HEAD_INSERTED";

	private BlockCSSStringTransformer cssBlockTrans = 
		new BlockCSSStringTransformer();
	private InlineCSSStringTransformer cssInlineTrans = 
		new InlineCSSStringTransformer();
	private StringTransformer jsBlockTrans =
		new JSStringTransformer();
	private MetaRefreshUrlStringTransformer metaRefreshTrans = 
		new MetaRefreshUrlStringTransformer();
	private URLStringTransformer anchorUrlTrans = new URLStringTransformer();

	protected String headInsertJsp = null;
	
//	static {
//		anchorUrlTrans = new URLStringTransformer();
//		anchorUrlTrans.setJsTransformer(jsBlockTrans);
//	}
	private static URLStringTransformer framesetUrlTrans =
		new URLStringTransformer("fw_");
	private static URLStringTransformer iframeUrlTrans =
		new URLStringTransformer("if_");	
	private static URLStringTransformer cssUrlTrans =
		new URLStringTransformer("cs_");
	private static URLStringTransformer jsUrlTrans =
		new URLStringTransformer("js_");
	private static URLStringTransformer imageUrlTrans =
		new URLStringTransformer("im_");
	private static URLStringTransformer objectEmbedUrlTrans =
		new URLStringTransformer("oe_");
	
	/** Constructor... */
	public FastArchivalUrlReplayParseEventHandler() {
		okHeadTagMap = new HashMap<String, Object>(okHeadTags.length);
		for (String tag : okHeadTags) {
			okHeadTagMap.put(tag, null);
		}
		anchorUrlTrans.setJsTransformer(jsBlockTrans);
	}
	
	// TODO: This should all be refactored up into an abstract base class with
	// default no-op methods, allowing a subclass to only override the ones they
	// want...
	public void handleNode(ParseContext pContext, Node node) 
	throws IOException {
		ReplayParseContext context = (ReplayParseContext) pContext;
		if(NodeUtils.isRemarkNode(node)) {
			RemarkNode remarkNode = (RemarkNode) node;
			remarkNode.setText(jsBlockTrans.transform(context, remarkNode.getText()));
			emit(context,null,node,null);

		} else if(NodeUtils.isTextNode(node)) {
			TextNode textNode = (TextNode) node;
			if(context.isInCSS()) {
				handleCSSTextNode(context,textNode);
				
			} else if(context.isInScriptText()) {
				handleJSTextNode(context,textNode);
			} else {
				emit(context,null,textNode,null);
//				handleContentTextNode(context,textNode);
			}
		} else if(NodeUtils.isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			
			if (NodeUtils.isOpenTagNodeNamed(tagNode, NodeUtils.SCRIPT_TAG_NAME)) {
				handleJSIncludeNode(context, tagNode);			
			} else if(tagNode.isEndTag()) {
				
				if (tagNode.getTagName().equals("HEAD")) {
					context.putData(FERRET_IN_HEAD, null);	
				}
				
				if (checkAllowTag(pContext, tagNode)) {
					emit(context,null,tagNode,null);
				}
					
//				handleCloseTagNode(context,tagNode);
			} else {
				// assume start, possibly empty:
				handleOpenTagNode(context,tagNode);
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
		emit(context,null,textNode,null);
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
		
		emit(context,null,textNode,null);
	}
	
	private void handleJSIncludeNode(ReplayParseContext context, TagNode tagNode) throws IOException {
		String file = tagNode.getAttribute("SRC");
		if (file != null) {
			//TODO: This is hacky.. fix it
			// This is used to check if the file should be skipped...
			//from a custom rule..
			String result = jsBlockTrans.transform(context, file);
			//The rewriting is done by the js_ rewriter
			if ((result != null) && !result.isEmpty()) {
				tagNode.setAttribute("SRC", jsUrlTrans.transform(context, file));				
			} else {
				file = "";
				tagNode.setAttribute("SRC", jsUrlTrans.transform(context, file));
			}
		}
		
		emit(context,null,tagNode,null);
	}

	private void handleOpenTagNode(ReplayParseContext context, TagNode tagNode) 
	throws IOException {
		
		boolean insertedJsp = context.getData(FERRET_DONE_KEY) != null;
		
		String preEmit = null;
		String postEmit = null;

		String tagName = tagNode.getTagName();
		
		boolean alreadyInsertedHead = (context.getData(FERRET_HEAD_INSERTED) != null);
		
		boolean inHead = (context.getData(FERRET_IN_HEAD) != null);

		if (!alreadyInsertedHead) {
			// If we're at the beginning of a <head> tag, and haven't inserted yet, 
			// insert right AFTER head tag
			if (tagName.equals("HEAD")) {
				emitHeadInsert(context, tagNode, true);
				context.putData(FERRET_IN_HEAD, FERRET_IN_HEAD);
				return;
			}
				
			
			// If we're at the beginning of any tag, other than <html>,
			// (including <body>) and haven't inserted yet,
			// insert right BEFORE the next tag, also continue other default processing
			// of the tag
			if (!tagName.equals("HTML") && !tagName.equals("!DOCTYPE")) {
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
		if(!insertedJsp && !context.isInCSS() && !context.isInScriptText() && !inHead) {
			if(!okHeadTagMap.containsKey(tagName)) {
				if(tagName.equals(FRAMESET_TAG)) {
					// don't put the insert in framsets:
				} else {
					if(jspInsertPath != null && !context.getJspExec().getUiResults().getWbRequest().isIFrameWrapperContext()) {
						String tmp = null; 
						try {
							tmp = 
								context.getJspExec().jspToString(jspInsertPath);
						} catch (ServletException e) {
							e.printStackTrace();
						}
						if (tagName.equals(BODY_TAG)) {
							// insert it now, *after* the current Tag:
							postEmit = tmp;
						} else {
							// hrm... we are seeing a node that should be in
							// the body.. lets emit the jsp now, *before*
							// the current Tag:
							preEmit = tmp;
						}
					}
				}
				context.putData(FERRET_DONE_KEY,"");
			}
		}
		
		// now do all the usual attribute rewriting:
		// this could be slightly optimized by moving tags more likely to occur
		// to the front of the if/else if/else if routing...

		if(tagName.equals("A")) {
			transformAttr(context, tagNode, "HREF", anchorUrlTrans);

		} else if(tagName.equals("APPLET")) {
			transformAttr(context, tagNode, "CODEBASE", objectEmbedUrlTrans);
			transformAttr(context, tagNode, "ARCHIVE", objectEmbedUrlTrans);

		} else if(tagName.equals("AREA")) {
			transformAttr(context, tagNode, "HREF", anchorUrlTrans);

		} else if(tagName.equals("BASE")) {
			String orig = tagNode.getAttribute("HREF"); 
			if(orig != null) {
				try {
					context.setBaseUrl(new URL(orig));
					transformAttr(context, tagNode, "HREF", anchorUrlTrans);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}

		} else if(tagName.equals("EMBED")) {
			transformAttr(context, tagNode, "SRC", objectEmbedUrlTrans);

		} else if(tagName.equals("IFRAME")) {
			transformAttr(context, tagNode, "SRC", iframeUrlTrans);

		} else if(tagName.equals("IMG")) {
			transformAttr(context, tagNode, "SRC", imageUrlTrans);

		} else if(tagName.equals("INPUT")) {
			transformAttr(context, tagNode, "SRC", imageUrlTrans);

		} else if(tagName.equals("FORM")) {
			transformAttr(context, tagNode, "ACTION", anchorUrlTrans);

		} else if(tagName.equals("FRAME")) {
			transformAttr(context, tagNode, "SRC", framesetUrlTrans);

		} else if(tagName.equals("LINK")) {
			if(transformAttrWhere(context, tagNode, "REL", "STYLESHEET", 
					"HREF",cssUrlTrans)) {
				// no-op
			} else if(transformAttrWhere(context,tagNode,"REL","SHORTCUT ICON",
					"HREF", imageUrlTrans)) {
				// no-op
			} else {
				transformAttr(context, tagNode, "HREF", anchorUrlTrans);
			}

		} else if(tagName.equals("META")) {
			transformAttrWhere(context, tagNode, "HTTP-EQUIV", "REFRESH",
					"CONTENT", metaRefreshTrans);
			transformAttr(context, tagNode, "URL", anchorUrlTrans);

		} else if(tagName.equals("OBJECT")) {
			transformAttr(context, tagNode, "CODEBASE", objectEmbedUrlTrans);
			transformAttr(context, tagNode, "CDATA", objectEmbedUrlTrans);

		} else if(tagName.equals("SCRIPT")) {
			transformAttr(context, tagNode, "SRC", jsUrlTrans);
		} else if(tagName.equals("DIV") || tagName.equals("LI")) {
			//HTML5 -- can have data-src or data-uri attributes in any tag!
			//Can really be in any tag but for now using most common use cases
			//Experimental
			transformAttr(context,tagNode,"data-src", objectEmbedUrlTrans);
			transformAttr(context,tagNode,"data-uri", objectEmbedUrlTrans);
		} else {
			if (!checkAllowTag(context, tagNode)) {
				return;
			}
		}
		// now, for *all* tags...
		transformAttr(context,tagNode,"BACKGROUND", imageUrlTrans);
		transformAttr(context,tagNode,"STYLE", cssInlineTrans);
		transformAttr(context,tagNode,"onclick", jsBlockTrans);
		transformAttr(context,tagNode,"onload", jsBlockTrans);
		transformAttr(context,tagNode,"onchange", jsBlockTrans);
		

		emit(context,preEmit,tagNode,postEmit);
	}
	
	protected boolean checkAllowTag(ParseContext context, TagNode tagNode)
	{
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
		if(out != null) {
//			Charset charset = Charset.forName(context.getOutputCharset());
			String charset = context.getOutputCharset();

			if(pre != null) {

				out.write(pre.getBytes(charset));
			}
			
			if (node != null) {
				out.write(node.toHtml(true).getBytes(charset));
			}

			if(post != null) {

				out.write(post.getBytes(charset));
			}
		}
	}
	
	/**
	 * Transform a particular attribute on a TagNode, if that TagNode has a
	 * previous value for the updated attribute, AND if that TagNode contains
	 * another named attribute with a specific value.
	 * 
	 * @param context the ReplayParseContext
	 * @param node the TagNode to be updated
	 * @param attrName update only occurs if the TagNode has an attribute with
	 * this name.
	 * @param attrVal update only occurs if the TagNode has an attribute 
	 * attrName has this value, case insensitive. In fact as an optimization,
	 * it is ASSUMED that this argument is already UPPER-CASED
	 * @param modAttr the attribute value to update
	 * @param transformer the StringTransformer responsible for creating the
	 * new value based on the old one.
	 * @return true if the attribute was updated.
	 */
	private boolean transformAttrWhere(ReplayParseContext context, TagNode node, 
			String attrName, String attrVal, String modAttr, 
			StringTransformer transformer) {
		String val = node.getAttribute(attrName);
		if(val != null) {
			if(val.toUpperCase().equals(attrVal)) {
				return transformAttr(context,node,modAttr,transformer);
			}
		}
		return false;
	}
	/**
	 * Transform a particular attribute on a TagNode, iff that attribute exists
	 * 
	 * @param context The ReplayParseContext being transformed
	 * @param node the TagNode to update
	 * @param attr the attribute name to transform
	 * @param transformer the StringTransformer responsible for creating the
	 * new value
	 * @return true if the attribute was found and updated
	 */
	private boolean transformAttr(ReplayParseContext context, TagNode node, 
			String attr, StringTransformer transformer) {
		String orig = node.getAttribute(attr);
		if(orig != null) {
			node.setAttribute(attr, 
					transformer.transform(context, orig));
			return true;
		}
		return false;
	}

	public void handleParseComplete(ParseContext pContext) throws IOException {
		if(endJsp != null) {
			ReplayParseContext context = (ReplayParseContext) pContext;
			OutputStream out = context.getOutputStream();
			String tmp = null; 
			try {
				tmp = context.getJspExec().jspToString(endJsp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			if(tmp != null) {
//				Charset charset = Charset.forName(context.getOutputCharset());
				String charset = context.getOutputCharset();
				out.write(tmp.getBytes(charset));
			}
		}
	}

	public void handleParseStart(ParseContext pContext) throws IOException {
		
		ReplayParseContext context = (ReplayParseContext) pContext;
		
		String policy = context.getJspExec().getUiResults().getResult().getOraclePolicy();
		
		if (policy != null) {
			context.setOraclePolicy(policy);
		}
		
		if(startJsp != null) {
			OutputStream out = context.getOutputStream();
			String tmp = null; 
			try {
				tmp = context.getJspExec().jspToString(startJsp);
			} catch (ServletException e) {
				e.printStackTrace();
			}
			if(tmp != null) {
//				Charset charset = Charset.forName(context.getOutputCharset());
				String charset = context.getOutputCharset();
				out.write(tmp.getBytes(charset));
			}
		}
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
        anchorUrlTrans.setJsTransformer(jsBlockTrans);
		
	}

	public String getHeadInsertJsp() {
		return headInsertJsp;
	}

	public void setHeadInsertJsp(String headInsertJsp) {
		this.headInsertJsp = headInsertJsp;
	}

	protected void emitHeadInsert(ReplayParseContext context, Node node, boolean postInsert)
			throws IOException {
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
}
