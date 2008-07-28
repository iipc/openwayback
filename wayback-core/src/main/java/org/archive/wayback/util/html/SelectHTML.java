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
