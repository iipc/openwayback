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
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;

import org.archive.wayback.util.DirMaker;

/**
 * Simple queue implementation, which uses a directory containing empty files
 * to indicate the presence of items in a queue (set in this case...)
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DirectoryIndexQueue implements IndexQueue {
	private File path = null;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.indexer.IndexQueue#dequeue()
	 */
	public String dequeue() throws IOException {
		String[] names = path.list();
		for(String name : names) {
			File tmp = new File(path,name);
			if(tmp.isFile()) {
				if(tmp.delete()) {
					return name;
				} else {
					throw new IOException("Unable to dequeue/delete (" + 
							tmp.getAbsolutePath());
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.indexer.IndexQueue#enqueue(java.lang.String)
	 */
	public void enqueue(String resourceFileName) throws IOException {
		File tmp = new File(path,resourceFileName);
		if(!tmp.isFile()) {
			tmp.createNewFile();
		}
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		if(path != null) {
			return path.getAbsolutePath();
		}
		return null;
	}

	/**
	 * @param path the path to set
	 * @throws IOException 
	 */
	public void setPath(String path) throws IOException {
		this.path = DirMaker.ensureDir(path);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourcestore.indexer.IndexQueue#recordStatus(java.lang.String, int)
	 */
	public void recordStatus(String resourceFileName, int status) {
		
	}
}
