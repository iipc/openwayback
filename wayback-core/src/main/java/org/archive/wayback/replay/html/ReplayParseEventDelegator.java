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

	public void handleParseStart(ParseContext context) throws IOException {
		preModifyDelegator.handleParseStart(context);
		modifyDelegator.handleParseStart(context);
		postModifyDelegator.handleParseStart(context);
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
