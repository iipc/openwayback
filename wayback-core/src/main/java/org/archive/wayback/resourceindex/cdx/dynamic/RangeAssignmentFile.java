/* RangeAssignmentFile
 *
 * $Id$
 *
 * Created on 3:48:02 PM Jan 25, 2007.
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
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.util.flatfile.FlatFile;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RangeAssignmentFile {
	private FlatFile cachedFile;
	
	/**
	 * @param cachedFile
	 */
	public RangeAssignmentFile(FlatFile cachedFile) {
		this.cachedFile = cachedFile;
	}
	/**
	 * @param nodeName
	 * @return String[] of Ranges assigned to nodeName
	 * @throws IOException
	 */
	public Object[] getRangesForNode(String nodeName) throws IOException {
		ArrayList<String> matches = new ArrayList<String>();
		Iterator<String> itr = cachedFile.getSequentialIterator();
		while(itr.hasNext()) {
			String line = (String) itr.next();
			if(line.indexOf(nodeName) > 0) {
				matches.add(line.substring(0,line.indexOf(' ')));
			}
		}
		return matches.toArray();
	}
}
