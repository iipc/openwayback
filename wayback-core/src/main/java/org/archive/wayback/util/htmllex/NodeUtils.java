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
