/* SelectHTML
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
package org.archive.wayback.util.html;

import java.util.ArrayList;
import java.util.List;

public class SelectHTML {
	List<String[]> options = null;
	String activeValue = null;
	String name = null;
	String props = null;
	public SelectHTML(String name) {
		this.name = name;
		options = new ArrayList<String[]>();
	}
	public void addOption(String name, String  value) {
		String[] newOption = {name,value};
		options.add(newOption);
	}
	public void addOption(String name) {
		addOption(name,name);
	}
	public void setActive(String value) {
		activeValue = value;
	}
	public void setProps(String props) {
		this.props = props;
	}
	public String draw() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("<select");
		if(props != null) {
			sb.append(" ").append(props);
		}
		sb.append(" name=\"").append(name).append("\">");
		
		for(String[] option : options) {
			sb.append("<option value=\"").append(option[1]).append("\"");
			if(activeValue != null) {
				if(activeValue.equals(option[1])) {
					sb.append(" selected");
				}
			}
			sb.append(">");
			sb.append(option[0]).append("</option>");
		}
		
		sb.append("</select>");
		return sb.toString();
	}
}
