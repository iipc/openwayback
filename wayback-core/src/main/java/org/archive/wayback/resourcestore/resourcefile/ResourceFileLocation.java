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
