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

import javax.servlet.ServletException;

import org.archive.wayback.replay.html.ReplayParseEventDelegator;
import org.archive.wayback.replay.html.ReplayParseEventDelegatorVisitor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.CloseTagHandler;
import org.archive.wayback.util.htmllex.handlers.ParseCompleteHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

public class BeforeBodyEndTagJSPExecRule extends JSPExecRule 
implements ReplayParseEventDelegatorVisitor, CloseTagHandler, ParseCompleteHandler  {
	private final static String FERRET_DONE_KEY = 
		BeforeBodyEndTagJSPExecRule.class.toString(); 

	public void visit(ReplayParseEventDelegator rules) {
		rules.getPreModifyDelegator().addCloseTagHandler(this);
		rules.getPreModifyDelegator().addParseCompleteHandler(this);
	}
	
	public void emit(ReplayParseContext context, Node node) throws IOException {
		String found = context.getData(FERRET_DONE_KEY);
		if(found == null) {
			context.putData(FERRET_DONE_KEY,"1");
			try {
				super.emit(context, node);
			} catch (ServletException e) {
				e.printStackTrace();
				throw new IOException(e.getMessage());
			}
		}
	}


	public void handleCloseTagNode(ParseContext context, TagNode node)
			throws IOException {
		String tagName = node.getTagName();
		if(tagName.equals("BODY") || tagName.equals("HTML")) {
			emit((ReplayParseContext) context,node);
		}
	}

	public void handleParseComplete(ParseContext context) throws IOException {
		emit((ReplayParseContext) context,null);
	}

}
