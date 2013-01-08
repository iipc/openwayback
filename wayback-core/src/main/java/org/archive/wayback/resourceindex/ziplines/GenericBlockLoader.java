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
import java.net.URISyntaxException;

/**
 * Generic BlockLoader, which may simplify configuration - inspecting each 
 * location to attempt to choose the correct BlockLoader:
 *   HDFS, HTTP, or LocalFile
 * @author brad
 *
 */
public class GenericBlockLoader implements BlockLoader {
	Http11BlockLoader http = null;
	HDFSBlockLoader hdfs = null;
	LocalFileBlockLoader local = null;
	private String defaultFSURI;
	
	int maxTotalConnections = -1;
	int maxHostConnections = -1;
	
	public GenericBlockLoader() {
		http = new Http11BlockLoader();
//		hdfs = new HDFSBlockLoader(null);
//		try {
//			hdfs.init();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		local = new LocalFileBlockLoader();
	}
	public void init() {
		if(defaultFSURI != null) {
			hdfs = new HDFSBlockLoader(defaultFSURI);
			try {
				hdfs.init();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (maxTotalConnections > 0) {
			http.setMaxTotalConnections(maxTotalConnections);
		}
		if (maxHostConnections > 0) {
			http.setMaxHostConnections(maxHostConnections);
		}
	}
	public byte[] getBlock(String url, long offset, int length)
			throws IOException {
		if(hdfs != null && url.startsWith("hdfs://")) {
			return hdfs.getBlock(url, offset, length);
		} else if(url.startsWith("/")) {
			return local.getBlock(url, offset, length);
		}
		return http.getBlock(url, offset, length);
	}
	public void setDefaultFSURI(String uri) {
		defaultFSURI = uri;
//		hdfs.setDefaultFSURI(uri);
	}
	public int getMaxTotalConnections() {
		return maxTotalConnections;
	}
	public void setMaxTotalConnections(int maxTotalConnections) {
		this.maxTotalConnections = maxTotalConnections;
	}
	public int getMaxHostConnections() {
		return maxHostConnections;
	}
	public void setMaxHostConnections(int maxHostConnections) {
		this.maxHostConnections = maxHostConnections;
	}
}
