/* ZiplinedBlockIndex
 *
 * $Id$:
 *
 * Created on May 14, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 * @author brad
 *
 */
public class ZiplinedBlockStringSequence {
	private FlatFile chunkIndex = null;
	private HashMap<String,String> chunkMap = null;
	private int maxBlocks = 10000;

	public ZiplinedBlockStringSequence(FlatFile chunkIndex, 
			HashMap<String,String> chunkMap) {
		this.chunkIndex = chunkIndex;
		this.chunkMap = chunkMap;
	}

	private ZiplineBlockMatches getBlockMatches(String prefix)
	throws IOException, ResourceIndexNotAvailableException {
		ArrayList<ZiplinedBlock> blocks = new ArrayList<ZiplinedBlock>();
		boolean first = true;
		int numBlocks = 0;
		boolean truncated = false;
		CloseableIterator<String> itr = null;
		try {
			itr = chunkIndex.getRecordIteratorLT(prefix);
			while(itr.hasNext()) {
				if(numBlocks >= maxBlocks) {
					truncated = true;
					break;
				}
				String blockDescriptor = itr.next();
				numBlocks++;
				String parts[] = blockDescriptor.split("\t");
				if(parts.length != 4) {
					throw new ResourceIndexNotAvailableException("Bad line(" + 
							blockDescriptor + ")");
				}
				// only compare the correct length:
				String prefCmp = prefix;
				String blockCmp = parts[0];
				if(first) {
					// always add first:
					first = false;
				} else if(!blockCmp.startsWith(prefCmp)) {
					// all done;
					break;
				}
				// add this and keep lookin...
				String url = chunkMap.get(parts[1]);
				long offset = Long.parseLong(parts[2]);
				int count = Integer.parseInt(parts[3]);
				
				blocks.add(new ZiplinedBlock(url, offset, count));
			}
		} finally {
			if(itr != null) {
				itr.close();
			}
		}
		return new ZiplineBlockMatches(blocks,prefix);
	}

	public StringPrefixIterator getIterator(String prefix, long skip)
	throws ResourceIndexNotAvailableException, IOException {
		ZiplineBlockMatches matches = getBlockMatches(prefix);
		return matches.getIteratorAt(skip);
	}
	public StringPrefixIterator getIterator(String prefix)
	throws ResourceIndexNotAvailableException, IOException {
		ZiplineBlockMatches matches = getBlockMatches(prefix);
		return matches.getIterator();
	}
}
