/* DirectoryIndexQueue
 *
 * $Id$
 *
 * Created on 2:29:10 PM Jun 23, 2008.
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
