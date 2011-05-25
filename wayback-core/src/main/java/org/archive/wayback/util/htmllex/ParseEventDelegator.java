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
import org.archive.wayback.util.htmllex.handlers.ParseStartHandler;
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
	private List<ParseStartHandler> parseStartHandlers = null;

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

	public void addParseStartHandler(ParseStartHandler v) {
		if(parseStartHandlers == null) {
			parseStartHandlers = new ArrayList<ParseStartHandler>();
		}
		parseStartHandlers.add(v);
	}
	public void handleParseStart(ParseContext context) throws IOException {
		if(parseStartHandlers != null) {
			for(ParseStartHandler v : parseStartHandlers) {
				v.handleParseStart(context);
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
