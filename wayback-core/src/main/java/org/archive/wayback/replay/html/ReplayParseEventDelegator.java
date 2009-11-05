/* ReplayParseEventDelegator
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
package org.archive.wayback.replay.html;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.archive.wayback.util.htmllex.ParseEventHandler;
import org.archive.wayback.util.htmllex.ParseEventDelegator;
import org.archive.wayback.util.htmllex.ParseContext;
import org.htmlparser.Node;

public class ReplayParseEventDelegator implements ParseEventHandler {
	
	public static final int PHASE_PRE_MODIFY = 0;
	public static final int PHASE_MODIFY = 1;
	public static final int PHASE_POST_OUTPUT = 2;

	private ParseEventDelegator preModifyDelegator = null;
	private ParseEventDelegator modifyDelegator = null;
	private ParseEventDelegator postModifyDelegator = null;
	private List<ReplayParseEventDelegatorVisitor> parserVisitors = null;
	
	protected void emit(ParseContext context, Node node) throws IOException {
		ReplayParseContext rContext = (ReplayParseContext) context;
		OutputStream out = rContext.getOutputStream();
		// no-op, override to actually output something:
		if(out != null) {
			String charset = rContext.getOutputCharset();
			String rawHTML = node.toHtml(true);
			byte[] bytes = null;
			try {
				bytes = rawHTML.getBytes(charset);
			} catch (UnsupportedEncodingException e) {
				bytes = rawHTML.getBytes();
			}
			out.write(bytes);
		}
	}


	public void init() {
		preModifyDelegator = new ParseEventDelegator();
		modifyDelegator = new ParseEventDelegator();
		postModifyDelegator = new ParseEventDelegator();
		if(parserVisitors != null) {
			for(ReplayParseEventDelegatorVisitor visitor : parserVisitors) {
				visitor.visit(this);
			}
		}
	}

	
	public void handleNode(ParseContext pContext, Node node) 
		throws IOException {
		ReplayParseContext context = (ReplayParseContext) pContext;
		context.setPhase(PHASE_PRE_MODIFY);
		preModifyDelegator.handleNode(context,node);
		context.setPhase(PHASE_MODIFY);
		modifyDelegator.handleNode(context,node);
		emit(context, node);
		context.setPhase(PHASE_POST_OUTPUT);
		postModifyDelegator.handleNode(context,node);


	}

	public void handleParseComplete(ParseContext context) throws IOException {
		preModifyDelegator.handleParseComplete(context);
		modifyDelegator.handleParseComplete(context);
		postModifyDelegator.handleParseComplete(context);
	}
	/**
	 * @return the preModifyDelegator
	 */
	public ParseEventDelegator getPreModifyDelegator() {
		return preModifyDelegator;
	}


	/**
	 * @param preModifyDelegator the preModifyDelegator to set
	 */
	public void setPreModifyDelegator(ParseEventDelegator preModifyDelegator) {
		this.preModifyDelegator = preModifyDelegator;
	}


	/**
	 * @return the modifyDelegator
	 */
	public ParseEventDelegator getModifyDelegator() {
		return modifyDelegator;
	}


	/**
	 * @param modifyDelegator the modifyDelegator to set
	 */
	public void setModifyDelegator(ParseEventDelegator modifyDelegator) {
		this.modifyDelegator = modifyDelegator;
	}


	/**
	 * @return the postModifyDelegator
	 */
	public ParseEventDelegator getPostModifyDelegator() {
		return postModifyDelegator;
	}


	/**
	 * @param postModifyDelegator the postModifyDelegator to set
	 */
	public void setPostModifyDelegator(ParseEventDelegator postModifyDelegator) {
		this.postModifyDelegator = postModifyDelegator;
	}


	/**
	 * @param parserVisitors the parserVisitors to set
	 */
	public void setParserVisitors(List<ReplayParseEventDelegatorVisitor> parserVisitors) {
		this.parserVisitors = parserVisitors;
	}
}
