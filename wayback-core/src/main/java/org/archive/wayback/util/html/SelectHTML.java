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
