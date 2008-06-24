/* ResourceFileLocation
 *
 * $Id$
 *
 * Created on 12:16:04 PM Jun 16, 2008.
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
package org.archive.wayback.resourcestore.resourcefile;

/**
 * Class encapsulating the name and String location(url/path) of a ResourceFile.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileLocation {
	private final static char DELIMETER = '\t';
	private String name = null;
	private String url = null;
	public ResourceFileLocation(String name, String url) {
		this.name = name;
		this.url = url;
	}
	public String serializeLine() {
		StringBuilder sb = new StringBuilder(100);
		sb.append(name);
		sb.append(DELIMETER);
		sb.append(url);
		return sb.toString();
	}
	public static ResourceFileLocation deserializeLine(String line) {
		int idx = line.indexOf(DELIMETER);
		if(idx > -1) {
			return new ResourceFileLocation(line.substring(0,idx),
					line.substring(idx+1));
		}
		return null;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}
