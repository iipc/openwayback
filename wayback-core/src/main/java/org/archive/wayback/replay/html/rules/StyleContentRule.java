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
package org.archive.wayback.replay.html.rules;

import java.io.IOException;

import org.archive.wayback.replay.html.ReplayParseEventDelegator;
import org.archive.wayback.replay.html.ReplayParseEventDelegatorVisitor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.CSSTextHandler;
import org.htmlparser.nodes.TextNode;

public class StyleContentRule implements ReplayParseEventDelegatorVisitor, CSSTextHandler {
	private StringTransformer transformer;

	public void visit(ReplayParseEventDelegator rules) {
		rules.getModifyDelegator().addCSSTextHandler(this);
	}
	public void handleCSSTextNode(ParseContext context, TextNode node)
	throws IOException {
		node.setText(transformer.transform((ReplayParseContext)context, node.getText()));
	}
	/**
	 * @return the transformer
	 */
	public StringTransformer getTransformer() {
		return transformer;
	}

	/**
	 * @param transformer the transformer to set
	 */
	public void setTransformer(StringTransformer transformer) {
		this.transformer = transformer;
	}
}
