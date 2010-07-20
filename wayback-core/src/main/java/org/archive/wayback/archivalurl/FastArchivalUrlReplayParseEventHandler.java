/* FastArchivalUrlReplayParseEventHandler
 *
 * $Id$:
 *
 * Created on May 4, 2010.
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

package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
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
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

/**
 * Lean and mean ParseEventHandler implementing current best-known server-side
 * HTML rewrite rules, and should be much faster than the fully configurable
 * version.
 * 
 * @author brad
 * 
 */
public class FastArchivalUrlReplayParseEventHandler implements
		ParseEventHandler {

	private final static String FERRET_DONE_KEY = 
		FastArchivalUrlReplayParseEventHandler.class.toString();

	private String jspInsertPath = "/WEB-INF/replay/DisclaimChooser.jsp";
	private String commentJsp = "/WEB-INF/replay/ArchiveComment.jsp";

	private final String[] okHeadTags = { "![CDATA[*", "![CDATA[", "?", 
			"!DOCTYPE", "HTML",	"HEAD", "BASE", "LINK", "META", "TITLE", 
			"STYLE", "SCRIPT" };
	private HashMap<String, Object> okHeadTagMap = null;
	private final static String FRAMESET_TAG = "FRAMESET";
	private final static String BODY_TAG = "BODY";

	private static BlockCSSStringTransformer cssBlockTrans = 
		new BlockCSSStringTransformer();
	private static InlineCSSStringTransformer cssInlineTrans = 
		new InlineCSSStringTransformer();
	private static JSStringTransformer jsBlockTrans =
		new JSStringTransformer();
	private static MetaRefreshUrlStringTransformer metaRefreshTrans = 
		new MetaRefreshUrlStringTransformer();
	private static URLStringTransformer anchorUrlTrans =
		new URLStringTransformer();
	private static URLStringTransformer cssUrlTrans =
		new URLStringTransformer("cs_");
	private static URLStringTransformer jsUrlTrans =
		new URLStringTransformer("js_");
	private static URLStringTransformer imageUrlTrans =
		new URLStringTransformer("im_");
	
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
	public void handleNode(ParseContext pContext, Node node) 
	throws IOException {
		ReplayParseContext context = (ReplayParseContext) pContext;
		if(NodeUtils.isRemarkNode(node)) {
//			RemarkNode remarkNode = (RemarkNode) node;
//			handleRemarkTextNode(context,remarkNode);
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
			if(tagNode.isEndTag()) {
				emit(context,null,tagNode,null);
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
		textNode.setText(jsBlockTrans.transform(context, textNode.getText()));
		emit(context,null,textNode,null);
	}

	private void handleOpenTagNode(ReplayParseContext context, TagNode tagNode) 
	throws IOException {
		
		boolean insertedJsp = context.getData(FERRET_DONE_KEY) != null;
		String preEmit = null;
		String postEmit = null;

		String tagName = tagNode.getTagName();
		// Time to insert the JSP header?
		if(!insertedJsp) {
			if(!okHeadTagMap.containsKey(tagName)) {
				if(tagName.equals(FRAMESET_TAG)) {
					// don't put the insert in framsets:
				} else {
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
				context.putData(FERRET_DONE_KEY,"");
			}
		}
		// now do all the usual attribute rewriting:
		// this could be slightly optimized by moving tags more likely to occur
		// to the front of the if/else if/else if routing...

		if(tagName.equals("A")) {
			transformAttr(context, tagNode, "HREF", anchorUrlTrans);

		} else if(tagName.equals("APPLET")) {
			transformAttr(context, tagNode, "CODEBASE", anchorUrlTrans);
			transformAttr(context, tagNode, "ARCHIVE", anchorUrlTrans);

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
			transformAttr(context, tagNode, "SRC", anchorUrlTrans);

		} else if(tagName.equals("IFRAME")) {
			transformAttr(context, tagNode, "SRC", anchorUrlTrans);

		} else if(tagName.equals("IMG")) {
			transformAttr(context, tagNode, "SRC", imageUrlTrans);

		} else if(tagName.equals("INPUT")) {
			transformAttr(context, tagNode, "SRC", imageUrlTrans);

		} else if(tagName.equals("FORM")) {
			transformAttr(context, tagNode, "ACTION", anchorUrlTrans);

		} else if(tagName.equals("FRAME")) {
			transformAttr(context, tagNode, "SRC", anchorUrlTrans);

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
			transformAttr(context, tagNode, "CODEBASE", anchorUrlTrans);
			transformAttr(context, tagNode, "CDATA", anchorUrlTrans);

		} else if(tagName.equals("SCRIPT")) {
			transformAttr(context, tagNode, "SRC", jsUrlTrans);
		}
		// now, for *all* tags...
		transformAttr(context,tagNode,"BACKGROUND", imageUrlTrans);
		transformAttr(context,tagNode,"STYLE", cssInlineTrans);
		transformAttr(context,tagNode,"onclick", jsBlockTrans);

		emit(context,preEmit,tagNode,postEmit);
	}

	private void emit(ReplayParseContext context, String pre, Node node, 
			String post) throws IOException {
		
		OutputStream out = context.getOutputStream();
		if(out != null) {
//			Charset charset = Charset.forName(context.getOutputCharset());
			String charset = context.getOutputCharset();

			if(pre != null) {

				out.write(pre.getBytes(charset));
			}

			out.write(node.toHtml(true).getBytes(charset));

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
		if(commentJsp != null) {
			ReplayParseContext context = (ReplayParseContext) pContext;
			OutputStream out = context.getOutputStream();
			String tmp = null; 
			try {
				tmp = context.getJspExec().jspToString(commentJsp);
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
	 */
	public String getCommentJsp() {
		return commentJsp;
	}

	/**
	 * @param commentJsp the commentJsp to set
	 */
	public void setCommentJsp(String commentJsp) {
		this.commentJsp = commentJsp;
	}
}
