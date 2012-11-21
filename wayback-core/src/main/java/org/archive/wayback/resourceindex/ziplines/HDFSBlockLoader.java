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
package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSBlockLoader implements BlockLoader {
	FileSystem fs = null;
	String defaultFSURI = null;
	public HDFSBlockLoader(String defaultFSURI) {
		this.defaultFSURI = defaultFSURI;
	}
	public void init() throws IOException, URISyntaxException {
		if(defaultFSURI == null) {
			Configuration c = new Configuration();
//			String defaultURI = "hdfs://hadoop-name.example.org/";
//			c.set("fs.default.name",defaultURI);
//			fs = FileSystem.get(new URI(defaultURI),c);		
			fs = FileSystem.get(c);
		} else {
			Configuration c = new Configuration();
			c.set("fs.default.name",defaultFSURI);
			fs = FileSystem.get(new URI(defaultFSURI),c);
		}
	}

	public byte[] getBlock(String url, long offset, int length)
			throws IOException {
		Path path = new Path(url);
		FSDataInputStream s = fs.open(path);
		byte buffer[] = new byte[length];
		try {
			s.readFully(offset, buffer);
		} finally {
			s.close();
		}
		return buffer;
	}

	/**
	 * @return the defaultFSURI
	 */
	public String getDefaultFSURI() {
		return defaultFSURI;
	}

	/**
	 * @param defaultFSURI the defaultFSURI to set
	 */
	public void setDefaultFSURI(String defaultFSURI) {
		this.defaultFSURI = defaultFSURI;
		Configuration c = new Configuration();
		c.set("fs.default.name",defaultFSURI);
		try {
			fs = FileSystem.get(new URI(defaultFSURI),c);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void finalize()
	{
		if (fs != null) {
			try {
				fs.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
