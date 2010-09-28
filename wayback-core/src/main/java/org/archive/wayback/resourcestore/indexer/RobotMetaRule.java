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
