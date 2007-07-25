/* MD5LocationFile
 *
 * $Id$
 *
 * Created on 3:47:19 PM Jan 25, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.cdx.dynamic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.archive.wayback.util.flatfile.FlatFile;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MD5LocationFile {
	private FlatFile cachedFile;
	
	/**
	 * @param cachedFile
	 */
	public MD5LocationFile(FlatFile cachedFile) {
		this.cachedFile = cachedFile;
	}
	/**
	 * @param md5
	 * @return array of String locations where sources of the MD5 can be found. 
	 * @throws IOException
	 */
	public Object[] getLocationsForMD5(String md5) throws IOException {
		HashMap<String,Object> matches = new HashMap<String,Object>();
		Iterator<String> itr = cachedFile.getSequentialIterator();
		while(itr.hasNext()) {
			String line = (String) itr.next();
			if(line.startsWith(md5)) {
				String locations[] = line.substring(md5.length()+1).split(" ");
				for(int i = 0; i<locations.length; i++) {
					if(!matches.containsKey(locations[i])) {
						matches.put(locations[i],null);
					}
				}
			}
		}
		return matches.keySet().toArray();
	}
}
