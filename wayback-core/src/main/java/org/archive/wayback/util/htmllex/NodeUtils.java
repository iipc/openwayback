/* NodeUtils
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
package org.archive.wayback.util.htmllex;

import org.htmlparser.Node;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;

public class NodeUtils {
	public static final String SCRIPT_TAG_NAME = "SCRIPT";
	public static final String STYLE_TAG_NAME = "STYLE";
	
	public static boolean isTagNode(Node node) {
		return (node instanceof TagNode);
	}
	public static boolean isTextNode(Node node) {
		return (node instanceof TextNode);
	}
	public static boolean isRemarkNode(Node node) {
		return (node instanceof RemarkNode);
	}
	public static boolean isTagNodeNamed(Node node, String name) {
		if(isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			String nodeName = tagNode.getTagName();
			return nodeName.equals(name);
		}
		return false;
	}
	public static boolean isOpenTagNodeNamed(Node node, String name) {
		if(isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			if(!tagNode.isEndTag()) {
				String nodeName = tagNode.getTagName();
				return nodeName.equals(name);
			}
		}
		return false;
	}
	public static boolean isNonEmptyOpenTagNodeNamed(Node node, String name) {
		if(isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			if(!tagNode.isEndTag() && !tagNode.isEmptyXmlTag()) {
				String nodeName = tagNode.getTagName();
				return nodeName.equals(name);
			}
		}
		return false;
	}
	public static boolean isCloseTagNodeNamed(Node node, String name) {
		if(isTagNode(node)) {
			TagNode tagNode = (TagNode) node;
			if(tagNode.isEndTag()) {
				String nodeName = tagNode.getTagName();
				return nodeName.equals(name);
			}
		}
		return false;
	}
}
