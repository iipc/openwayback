/* ZiplineBlockMatches
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
