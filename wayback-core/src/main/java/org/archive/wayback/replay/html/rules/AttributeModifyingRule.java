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
package org.archive.wayback.replay.html.rules;

import java.io.IOException;

import org.archive.wayback.replay.html.ReplayParseEventDelegator;
import org.archive.wayback.replay.html.ReplayParseEventDelegatorVisitor;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.handlers.OpenTagHandler;
import org.htmlparser.nodes.TagNode;

public class AttributeModifyingRule implements ReplayParseEventDelegatorVisitor, 
	OpenTagHandler {

	private String tagName = null;
	private String whereAttributeName = null;
	private String whereAttributeValue = null;
	private String modifyAttributeName = null;
	private StringTransformer transformer;
	
	public void visit(ReplayParseEventDelegator rules) {
		if(modifyAttributeName == null) {
			throw new RuntimeException("Need modifyAttributeName");
		}
		if(tagName == null) {
			rules.getModifyDelegator().addOpenTagHandler(this);
		} else {
			rules.getModifyDelegator().addOpenTagHandler(this, tagName);
		}
	}

	public void handleOpenTagNode(ParseContext context, TagNode node)
			throws IOException {
		if(whereAttributeName != null) {
			// if matchAttrName is set, make sure it is present:
			String nodeAttrVal = node.getAttribute(whereAttributeName);
			if(nodeAttrVal == null) {
				return;
			}
			// if the value is specified, too, make sure that matches, as well:
			if(whereAttributeValue != null) {
				if(!nodeAttrVal.toUpperCase().equals(whereAttributeValue)) {
					return;
				}
			}
		}
		// try to perform the update:
		if(modifyAttributeName == null) {
			// mis-configuration... this is required:
			// TODO: log a warning
			return;
		}
		String nodeVal = node.getAttribute(modifyAttributeName);
		if(nodeVal != null) {
			String newVal = transformer.transform((ReplayParseContext)context, nodeVal);
			node.setAttribute(modifyAttributeName, newVal);
		}
	}

	/**
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * @param tagName the tagName to set
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName.toUpperCase();
	}

	/**
	 * @return the whereAttributeName
	 */
	public String getWhereAttributeName() {
		return whereAttributeName;
	}

	/**
	 * @param whereAttributeName the whereAttributeName to set
	 */
	public void setWhereAttributeName(String whereAttributeName) {
		this.whereAttributeName = whereAttributeName.toUpperCase();
	}

	/**
	 * @return the whereAttributeValue
	 */
	public String getWhereAttributeValue() {
		return whereAttributeValue;
	}

	/**
	 * @param whereAttributeValue the whereAttributeValue to set
	 */
	public void setWhereAttributeValue(String whereAttributeValue) {
		this.whereAttributeValue = whereAttributeValue.toUpperCase();
	}

	/**
	 * @return the modifyAttributeName
	 */
	public String getModifyAttributeName() {
		return modifyAttributeName;
	}

	/**
	 * @param modifyAttribute the modifyAttribute to set
	 */
	public void setModifyAttributeName(String modifyAttributeName) {
		this.modifyAttributeName = modifyAttributeName.toUpperCase();
	}

	/**
	 * @return the transformer
	 */
	public StringTransformer getTransformer() {
		return transformer;
	}

	/**
	 * @param transformer the transformer to set
	 */
	public void setTransformer(StringTransformer transformer) {
		this.transformer = transformer;
	}
}
