/* AfterBodyStartTagJSPExecRule
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
