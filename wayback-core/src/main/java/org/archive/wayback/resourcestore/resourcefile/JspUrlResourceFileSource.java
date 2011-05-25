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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.archive.wayback.util.ByteOp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class JspUrlResourceFileSource implements ResourceFileSource {
	
	private final static char WEB_SEPARATOR_CHAR = '/';
	private final static String LINE_SEPARATOR_STRING = "\n";
	private String name = null;
	private String prefix = null;
	private String jsp = null;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getBasename(java.lang.String)
	 */
	public String getBasename(String path) {
		int sepIdx = path.lastIndexOf(WEB_SEPARATOR_CHAR);
		if(sepIdx != -1) {
			return path.substring(sepIdx + 1);
		}
		return path;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getFileList()
	 */
	public ResourceFileList getResourceFileList() throws IOException {
		
		String url = "http://localhost:8080" + jsp + "?url=" + prefix;
		URL u = new URL(url);
		InputStream is = u.openStream();
		InputStreamReader isr = new InputStreamReader(is,ByteOp.UTF8);
		StringBuilder sb = new StringBuilder(2000);
		int READ_SIZE = 2048;
		char cbuf[] = new char[READ_SIZE];
		int amt = 0;
		while((amt = isr.read(cbuf, 0, READ_SIZE)) != -1) {
			sb.append(new String(cbuf,0,amt));
		}
		ResourceFileList list = new ResourceFileList();
		String lines[] = sb.toString().split(LINE_SEPARATOR_STRING);
		for(String line : lines) {
			ResourceFileLocation location = 
				ResourceFileLocation.deserializeLine(line);
			if(location != null) {
				list.add(location);
			} else {
				throw new IOException("Bad line format(" + line +")");
			}
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.resourcefile.ResourceFileSource#getPrefix()
	 */
	public String getPrefix() {
		return prefix;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getJsp() {
		return jsp;
	}

	public void setJsp(String jsp) {
		this.jsp = jsp;
	}
}
