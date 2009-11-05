/* ParseEventDelegator
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util.htmllex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archive.wayback.util.htmllex.handlers.CSSTextHandler;
import org.archive.wayback.util.htmllex.handlers.CloseTagHandler;
import org.archive.wayback.util.htmllex.handlers.ContentTextHandler;
import org.archive.wayback.util.htmllex.handlers.JSTextHandler;
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.archive.wayback.util.htmllex.handlers.ParseCompleteHandler;
import org.archive.wayback.util.htmllex.handlers.RemarkTextHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

/**
 *
 * This class provides an abstraction between high-level SAX events, and
 * application specific low-level SAX event handlers.
 * 
 * Any object which wishes to receive any low-level SAX events is placed in the
 * parserVisitors List, and at initialization of this class, each element in
 * that list is given an opportunity to register to receive whatever low-level
 * SAX events it is interested in.
 * 
 * This class also manages casting of Node objects into more event-specific
 * casts, and uses the ParseContext to route specific nodes to the registered
 * handlers of each low-level event types. 
 * 
 * This class attempts to be efficient about targeting specific TagNodes:
 *   When registering to receive events, handlers can register for a specific
 *   tag name, or for the global-tag ("*") name.
 * 
 * As TagNodes are handled, all tag-specific handlers are called, followed by
 * all global-tag handlers.
 *
 * @author brad
 */
public class ParseEventDelegator implements ParseEventHandler {

	public static final String WILDCARD_TAG_NAME = "*";

	private Map<String,List<CloseTagHandler>> closeTagHandlers = null;
	private Map<String,List<OpenTagHandler>> openTagHandlers = null;
	private List<CSSTextHandler> cssTextHandlers = null;
	private List<JSTextHandler> jsTextHandler = null;
	private List<RemarkTextHandler> remarkTextHandler = null;
	private List<ContentTextHandler> contentTextHandler = null;
	private List<ParseCompleteHandler> parseCompleteHandlers = null;

	private List<ParseEventDelegatorVisitor> parserVisitors = null;


	public void init() {
		if(parserVisitors != null) {
			for(ParseEventDelegatorVisitor visitor : parserVisitors) {
				visitor.visit(this);
			}
		}
	}

	public void handleNode(ParseContext context, Node node) 
		throws IOException {

		if(NodeUtils.isRemarkNode(node)) {
			RemarkNode remarkNode = (RemarkNode) node;
			handleRemarkTextNode(context,remarkNode);
			
		} else if(NodeUtils.isTextNode(node)) {
			TextNode textNode = (TextNode) node;
			if(context.isInCSS()) {
				handleCSSTextNode(context,textNode);
				
			} else if(context.isInScriptText()) {
				handleJSTextNode(context,textNode);
			} else {
				handleContentTextNode(context,textNode);
			}
		} else if(NodeUtils.isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			if(tagNode.isEndTag()) {
				handleCloseTagNode(context,tagNode);
			} else {
				// assume start, possibly empty:
				handleOpenTagNode(context,tagNode);
			}
		} else {
			throw new IllegalArgumentException("Unknown node type..");
		}
	}

	// CLOSE TAG:
	public void addCloseTagHandler(CloseTagHandler v) {
		addCloseTagHandler(v, WILDCARD_TAG_NAME);
	}
	public void addCloseTagHandler(CloseTagHandler v, String name) {
		if(closeTagHandlers == null) {
			closeTagHandlers = new HashMap<String,List<CloseTagHandler>>();
		}
		if(!closeTagHandlers.containsKey(name)) {
			closeTagHandlers.put(name, new ArrayList<CloseTagHandler>());
		}
		closeTagHandlers.get(name).add(v);
	}
	public void handleCloseTagNode(ParseContext context, TagNode node) throws IOException {
		String name = node.getTagName();
		if(closeTagHandlers != null) {
			for(String n : new String[]{name,WILDCARD_TAG_NAME}) {
				if(closeTagHandlers.containsKey(n)) {
					for(CloseTagHandler v : closeTagHandlers.get(n)) {
						v.handleCloseTagNode(context,node);
					}
				}
			}
		}
	}

	// OPEN TAG:
	public void addOpenTagHandler(OpenTagHandler v) {
		addOpenTagHandler(v, WILDCARD_TAG_NAME);
	}
	public void addOpenTagHandler(OpenTagHandler v, String name) {
		if(openTagHandlers == null) {
			openTagHandlers = new HashMap<String,List<OpenTagHandler>>();
		}
		if(!openTagHandlers.containsKey(name)) {
			openTagHandlers.put(name, new ArrayList<OpenTagHandler>());
		}
		openTagHandlers.get(name).add(v);
	}

	public void handleOpenTagNode(ParseContext context, TagNode node) throws IOException {
		String name = node.getTagName();
		if(openTagHandlers != null) {
			for(String n : new String[]{name,WILDCARD_TAG_NAME}) {
				if(openTagHandlers.containsKey(n)) {
					for(OpenTagHandler v : openTagHandlers.get(n)) {
						v.handleOpenTagNode(context,node);
					}
				}
			}
		}
	}
	public void addCSSTextHandler(CSSTextHandler v) {
		if(cssTextHandlers == null) {
			cssTextHandlers = new ArrayList<CSSTextHandler>();
		}
		cssTextHandlers.add(v);
	}
	public void handleCSSTextNode(ParseContext context, TextNode node) throws IOException {
		if(cssTextHandlers != null) {
			for(CSSTextHandler v : cssTextHandlers) {
				v.handleCSSTextNode(context,node);
			}
		}
	}
	public void addJSTextHandler(JSTextHandler v) {
		if(jsTextHandler == null) {
			jsTextHandler = new ArrayList<JSTextHandler>();
		}
		jsTextHandler.add(v);
	}
	public void handleJSTextNode(ParseContext context, TextNode node) throws IOException {
		if(jsTextHandler != null) {
			for(JSTextHandler v : jsTextHandler) {
				v.handleJSTextNode(context,node);
			}
		}
	}

	public void addRemarkTextHandler(RemarkTextHandler v) {
		if(remarkTextHandler == null) {
			remarkTextHandler = new ArrayList<RemarkTextHandler>();
		}
		remarkTextHandler.add(v);
	}
	public void handleRemarkTextNode(ParseContext context, RemarkNode node) throws IOException {
		if(remarkTextHandler != null) {
			for(RemarkTextHandler v : remarkTextHandler) {
				v.handleRemarkTextNode(context,node);
			}
		}
	}

	public void addContentTextHandler(ContentTextHandler v) {
		if(contentTextHandler == null) {
			contentTextHandler = new ArrayList<ContentTextHandler>();
		}
		contentTextHandler.add(v);
	}
	public void handleContentTextNode(ParseContext context, TextNode node) throws IOException {
		if(contentTextHandler != null) {
			for(ContentTextHandler v : contentTextHandler) {
				v.handleContentTextNode(context,node);
			}
		}
	}

	public void addParseCompleteHandler(ParseCompleteHandler v) {
		if(parseCompleteHandlers == null) {
			parseCompleteHandlers = new ArrayList<ParseCompleteHandler>();
		}
		parseCompleteHandlers.add(v);
	}
	public void handleParseComplete(ParseContext context) throws IOException {
		if(parseCompleteHandlers != null) {
			for(ParseCompleteHandler v : parseCompleteHandlers) {
				v.handleParseComplete(context);
			}
		}
	}

	/**
	 * @return the parserVisitors
	 */
	public List<ParseEventDelegatorVisitor> getParserVisitors() {
		return parserVisitors;
	}

	/**
	 * @param parserVisitors the parserVisitors to set
	 */
	public void setParserVisitors(List<ParseEventDelegatorVisitor> parserVisitors) {
		this.parserVisitors = parserVisitors;
	}
}
