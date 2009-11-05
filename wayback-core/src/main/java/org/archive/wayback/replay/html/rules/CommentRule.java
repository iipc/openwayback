/* CommentRule
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
import java.io.OutputStream;

import org.archive.wayback.replay.html.ReplayParseEventDelegator;
import org.archive.wayback.replay.html.ReplayParseEventDelegatorVisitor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.CloseTagHandler;
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.htmlparser.Node;
import org.htmlparser.nodes.TagNode;

public class CommentRule implements ReplayParseEventDelegatorVisitor, 
	OpenTagHandler, CloseTagHandler {

	private final static byte[] startComment = "<!--".getBytes();
	private final static byte[] endComment = "-->".getBytes();
	
	public void emit(ReplayParseContext context, Node node) throws IOException {
		OutputStream os = context.getOutputStream();
		if(os != null) {
			os.write(startComment);
			os.write(node.toHtml(true).getBytes());
			os.write(endComment);
		}
	}

	public void visit(ReplayParseEventDelegator rules) {
		rules.getPreModifyDelegator().addOpenTagHandler(this);
		rules.getPreModifyDelegator().addCloseTagHandler(this, "A");
	}

	public void handleOpenTagNode(ParseContext context, TagNode node) throws IOException {
		emit((ReplayParseContext)context,node);
	}

	public void handleCloseTagNode(ParseContext context, TagNode node)
			throws IOException {
		emit((ReplayParseContext)context,node);
	}
}
