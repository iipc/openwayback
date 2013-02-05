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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileList {
	private static final Logger LOGGER =
        Logger.getLogger(ResourceFileList.class.getName());

	private HashMap<String,ResourceFileLocation> files = 
		new HashMap<String,ResourceFileLocation>();
	public void add(ResourceFileLocation location) {
		files.put(location.serializeLine(), location);
	}
	public void addAll(Iterator<ResourceFileLocation> itr) {
		while(itr.hasNext()) {
			add(itr.next());
		}
	}

	public Iterator<ResourceFileLocation> iterator() {
		return files.values().iterator();
	}

	public void store(File target) throws IOException {
		FlatFile ff = new FlatFile(target.getAbsolutePath());
		Iterator<String> adapted = 
			new AdaptedIterator<ResourceFileLocation,String>(iterator(),
				new ResourceFileLocationAdapter());
		ff.store(adapted);
	}

	public static ResourceFileList load(File source) throws IOException {
		ResourceFileList list = new ResourceFileList();
		
		FlatFile ff = new FlatFile(source.getAbsolutePath());
		CloseableIterator<String> itr = ff.getSequentialIterator();
		while(itr.hasNext()) {
			String line = itr.next();
			ResourceFileLocation location = 
				ResourceFileLocation.deserializeLine(line);
			if(location != null) {
				list.add(location);
			} else {
				LOGGER.warning("Bad parse of line(" + line + ") in (" + 
						source.getAbsolutePath() + ")");
			}
		}
		itr.close();
		return list;
	}

	public ResourceFileList subtract(ResourceFileList that) {
		HashMap<String,ResourceFileLocation> tmp = 
			new HashMap<String,ResourceFileLocation>();
		Iterator<ResourceFileLocation> thisItr = iterator();
		while(thisItr.hasNext()) {
			ResourceFileLocation location = thisItr.next();
			tmp.put(location.serializeLine(), location);
		}

		Iterator<ResourceFileLocation> thatItr = that.iterator();
		while(thatItr.hasNext()) {
			ResourceFileLocation location = thatItr.next();
			tmp.remove(location.serializeLine());
		}
		ResourceFileList sub = new ResourceFileList();
		sub.addAll(tmp.values().iterator());
		return sub;
	}

	private class ResourceFileLocationAdapter implements Adapter<ResourceFileLocation,String> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public String adapt(ResourceFileLocation o) {
			return o.serializeLine();
		}
	}
}
