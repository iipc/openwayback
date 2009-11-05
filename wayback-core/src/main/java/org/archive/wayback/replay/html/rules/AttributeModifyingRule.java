/* AttributeModifyingRule
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
				if(!nodeAttrVal.equals(whereAttributeValue)) {
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
		this.whereAttributeValue = whereAttributeValue;
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
