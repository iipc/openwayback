/* ResourceFileList
 *
 * $Id$
 *
 * Created on 12:15:53 PM Jun 16, 2008.
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;
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
				LOGGER.warn("Bad parse of line(" + line + ") in (" + 
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
