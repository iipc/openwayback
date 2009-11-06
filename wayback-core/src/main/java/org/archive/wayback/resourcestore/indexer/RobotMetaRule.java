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
