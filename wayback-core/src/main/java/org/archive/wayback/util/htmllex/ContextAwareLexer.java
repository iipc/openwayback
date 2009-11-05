/* ContextAwareLexer
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

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.util.ParserException;

/**
 * 
 * The Lexer that comes with htmlparser does not handle non-escaped HTML 
 * entities within SCRIPT tags - by default, something like:
 * 
 *    <script>
 *      for(var i=0; i<23; i++) { j+=i; }
 *    </script>
 * 
 * Can cause the lexer to skip over a large part of the document. Technically,
 * the above isn't legit HTML, but of course, folks do stuff like that all the
 * time. So, this class uses a ParseContext object, passed in at construction,
 * which observes the SCRIPT and STYLE tags, both setting properties on the
 * ParseContext, and using that state information to perform a parseCDATA()
 * call instead of a nextNode() call at the right time, to try to keep the
 * SAX parsing in sync with the document.
 * 
 * @author brad
 *
 */
public class ContextAwareLexer extends NodeUtils {

	private Lexer lexer = null;
	private ParseContext context = null;
	public ContextAwareLexer(Lexer lexer, ParseContext context) {
		this.lexer = lexer;
		this.context = context;
	}
	public Node nextNode() throws ParserException {
		Node node = null;
		if(context.isInJS()) {
			node = lexer.parseCDATA(true);
			if(node != null) {
				context.setInScriptText(true);
				context.setInJS(false);
				return node;
			}
		}
		context.setInScriptText(false);
		node = lexer.nextNode(context.isInJS());
		if(node != null) {
			if(isNonEmptyOpenTagNodeNamed(node, SCRIPT_TAG_NAME)) {
				context.setInJS(true);
			} else if(isCloseTagNodeNamed(node, SCRIPT_TAG_NAME)) {
				context.setInJS(false);
			} else if(isNonEmptyOpenTagNodeNamed(node, STYLE_TAG_NAME)) {
				context.setInCSS(true);
			} else if(isCloseTagNodeNamed(node, STYLE_TAG_NAME)) {
				context.setInCSS(false);
			}
		}
		return node;
	}
}
