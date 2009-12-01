/* BeforeBodyEndTagJSPExecRule
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
