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
		if (context.isInJS()) {
			node = lexer.parseCDATA(false);
			if (node != null) {
				context.setInScriptText(true);
				context.setInJS(false);
				return node;
			}
		} else if (context.isInScriptText()) {
			node = lexer.parseCDATA(false);
			if (node != null) {
				return node;
			}
		}
		node = lexer.nextNode(context.isInJS());
		if(node != null) {
			if(isNonEmptyOpenTagNodeNamed(node, SCRIPT_TAG_NAME)) {
				context.setInJS(true);
			} else if(isCloseTagNodeNamed(node, SCRIPT_TAG_NAME)) {
				context.setInJS(false);
				context.setInScriptText(false);
			} else if(isNonEmptyOpenTagNodeNamed(node, STYLE_TAG_NAME)) {
				context.setInCSS(true);
			} else if(isCloseTagNodeNamed(node, STYLE_TAG_NAME)) {
				context.setInCSS(false);
			}
		}
		return node;
	}
}
