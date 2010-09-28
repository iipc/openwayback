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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author brad
 *
 */
public class ZiplineBlockMatches {
	private ArrayList<ZiplinedBlock> blocks = null;
	private String prefix = null;
	private int cachedFirstCount = -1;
	private int cachedLastCount = -1;
	public ZiplineBlockMatches(ArrayList<ZiplinedBlock> blocks, String prefix) {
		this.blocks = blocks;
		this.prefix = prefix;
		cachedFirstCount = -1;
		cachedLastCount = -1;
	}

	public StringPrefixIterator getIterator() {
		ZiplinesChunkIterator zci = new ZiplinesChunkIterator(blocks);
		zci.setTruncated(false);
		return new StringPrefixIterator(zci,prefix);
	}
	
	public StringPrefixIterator getIteratorAt(long skip) throws IOException {
		SkippingStringPrefixIterator itr = null;
		ArrayList<ZiplinedBlock> matchingBlocked = 
			new ArrayList<ZiplinedBlock>();
		long total = getTotalMatching();
		if(skip > total) {
			// TODO: should return empty itr...
			return null;
		}
		long firstBlockMatches = 
			countMatchesInStartBlock(blocks.get(0), prefix);
		if(skip < firstBlockMatches) {
			ZiplinesChunkIterator zci = new ZiplinesChunkIterator(blocks);
			itr = new SkippingStringPrefixIterator(zci,prefix,skip);
			itr.setTotalMatches(total);
			return itr;
		}
		skip -= firstBlockMatches;
		int size = blocks.size();
		for(int i = 1; i < size; i++) {
			ZiplinedBlock block = blocks.get(i);
			if(block.count > skip) {
				// this is the block to start:
				ZiplinesChunkIterator zci = 
					new ZiplinesChunkIterator(blocks.subList(i, size));
				itr = new SkippingStringPrefixIterator(zci,prefix,skip);
				itr.setTotalMatches(total);
				return itr;
			}
			skip -= block.count;
		}
		// should never get here...
		return null;
	}
	
	public long getTotalMatching() throws IOException {
		if(blocks == null) {
			return 0;
		}
		int size = blocks.size();
		if(size == 0) {
			return 0;
		}
		long count = countMatchesInStartBlock(blocks.get(0),prefix);
		if(size == 1) {
			return count;
		}
		for(int i = 1; i < size-1; i++) {
			count += blocks.get(i).count;
		}
		count += countMatchesInLastBlock(blocks.get(size-1), prefix);
		return count;
	}
	private long countMatchesInStartBlock(ZiplinedBlock block, String prefix)
	throws IOException {
		if(cachedFirstCount == -1) {
			BufferedReader r = block.readBlock();
			int matches = block.count;
			while(true) {
				String nextLine = r.readLine();
				if((nextLine == null) || nextLine.startsWith(prefix)) {
					r.close();
					cachedFirstCount = matches;
					break;
				}
				matches--;
			}
		}
		return cachedFirstCount;
	}
	private long countMatchesInLastBlock(ZiplinedBlock block, String prefix)
	throws IOException {
		if(cachedLastCount == -1) {
			BufferedReader r = block.readBlock();
			int matches = 0;
			while(true) {
				String nextLine = r.readLine();
				if((nextLine == null) || !nextLine.startsWith(prefix)) {
					r.close();
					cachedLastCount = matches;
					break;
				}
				matches++;
			}
		}
		return cachedLastCount;
	}
}
