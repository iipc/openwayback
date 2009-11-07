/* RobotMetaRule
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.IOException;

import org.archive.wayback.util.htmllex.ParseEventDelegator;
import org.archive.wayback.util.htmllex.ParseEventDelegatorVisitor;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.htmlparser.nodes.TagNode;

public class RobotMetaRule implements ParseEventDelegatorVisitor, OpenTagHandler {

	private RobotMetaFlags robotFlags = null;
	
	public void visit(ParseEventDelegator rules) {
		// register for <META> Start tags:
		rules.addOpenTagHandler(this, "META");
	}

	public void handleOpenTagNode(ParseContext context, TagNode node)
			throws IOException {
		String nameVal = node.getAttribute("name");
		if(nameVal != null) {
			if(nameVal.toUpperCase().equals("ROBOTS")) {
				String content = node.getAttribute("content");
				if(content != null) {
					robotFlags.parse(content);
				}
			}
		}
	}

	/**
	 * @return the robotFlags
	 */
	public RobotMetaFlags getRobotFlags() {
		return robotFlags;
	}

	/**
	 * @param robotFlags the robotFlags to set
	 */
	public void setRobotFlags(RobotMetaFlags robotFlags) {
		this.robotFlags = robotFlags;
	}

}
