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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 * @author brad
 *
 */
public class ZiplinedBlockStringSequence {
	private static final Logger LOGGER = Logger.getLogger(
			ZiplinedBlockStringSequence.class.getName());

	private FlatFile chunkIndex = null;
	private HashMap<String,BlockLocation> chunkMap = null;
	private int maxBlocks = 10000;

	public ZiplinedBlockStringSequence(FlatFile chunkIndex, 
			HashMap<String,BlockLocation> chunkMap) {
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
					LOGGER.severe("Bad Block descriptor Line(" + 
							blockDescriptor + " in " + chunkIndex.getPath());
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
				BlockLocation bl = chunkMap.get(parts[1]);
				long offset = Long.parseLong(parts[2]);
				int count = Integer.parseInt(parts[3]);
				
				blocks.add(new ZiplinedBlock(bl.getLocations(), offset, count));
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
