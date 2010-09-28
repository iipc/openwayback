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
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

/**
 * This Rule fires just after the BODY start tag, emitting the result of the
 * replay .jsp into the resulting page at that point.
 * 
 * Sounds simple, BUT, it's possible there is no BODY start tag...
 * 
 * In case this happens, we watch *ALL* tags go by, before they've been output,
 * and if we see any start tags not of the following types:
 * 
 *   html,head,base,link,meta,title,style,script
 *   
 * we emit our content then and there.
 * 
 * We also ensure we don't emit twice by storing a flag in the ParseContext once
 * we do emit.
 * 
 * Lastly, if we see a "FRAMESET" tag in the page, we hold off on inserting 
 * our content.
 * 
 * @author brad
 *
 */
public class AfterBodyStartTagJSPExecRule extends JSPExecRule 
implements ReplayParseEventDelegatorVisitor, OpenTagHandler {
	private final String[] okHeadTags = {
		"![CDATA[*", "!DOCTYPE","HTML","HEAD","BASE","LINK","META","TITLE",
		"STYLE","SCRIPT"
	};
	private final static String FRAMESET_TAG = "FRAMESET";
	private final static String FERRET_DONE_KEY = 
		AfterBodyStartTagJSPExecRule.class.toString(); 
	public void visit(ReplayParseEventDelegator rules) {
		
		rules.getPostModifyDelegator().addOpenTagHandler(this,"BODY");
		rules.getPreModifyDelegator().addOpenTagHandler(this);
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
	
	private boolean isNotTagAppearingInHead(TagNode node) {
		String thisTag = node.getTagName();
		if(thisTag.startsWith("!")) return false;
		for(String tag : okHeadTags) {
			if(thisTag.equals(tag)) {
				return false;
			}
		}
		return true;
	}

	public void handleOpenTagNode(ParseContext pContext, TagNode node)
			throws IOException {
		ReplayParseContext context = (ReplayParseContext) pContext;
		if(context.getData(FERRET_DONE_KEY) == null) {
			// we haven't emitted yet:
			// are we running in post-emit?
			if(context.getPhase() == ReplayParseEventDelegator.PHASE_POST_OUTPUT) {
				// emit if it is a body tag:
				if(node.getTagName().equals("BODY")) {
					emit((ReplayParseContext) context,node);
				}
			} else {
				// must be PHASE_PRE_MODIFY: if it's a body tag, emit now:
				if(isNotTagAppearingInHead(node)) {
					if(node.getTagName().equals(FRAMESET_TAG)) {
						// don't put content in pages with a FRAMESET:
						context.putData(FERRET_DONE_KEY,"1");
					} else {
						// and this is a tag that shouldn't be in the HEAD. Emit:
						emit((ReplayParseContext) context,node);
					}
				}
			}
		}
	}
}
