/* JspUrlResourceFileSource
 *
 * $Id$
 *
 * Created on 5:05:53 PM Jun 5, 2008.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

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
		InputStreamReader isr = new InputStreamReader(is);
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
