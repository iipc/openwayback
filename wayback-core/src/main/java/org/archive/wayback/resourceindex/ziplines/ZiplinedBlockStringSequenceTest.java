/* ZiplinedBlockStringSequenceTest
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
import java.util.HashMap;

import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.flatfile.FlatFile;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ZiplinedBlockStringSequenceTest extends TestCase {
	private String indexPath = "/home/brad/os-cdx/CDX-201002-clean/ALL.count.summary";
	private String mapPath = "/home/brad/os-cdx/CDX-201002-clean/ALL.loc-workstation";

	private ZiplinedBlockStringSequence getSequence() throws IOException {
		HashMap<String, String> chunkMap = new HashMap<String, String>();
		FlatFile ff = new FlatFile(mapPath);
		CloseableIterator<String> lines = ff.getSequentialIterator();
		while(lines.hasNext()) {
			String line = lines.next();
			String[] parts = line.split("\\s");
			if(parts.length != 2) {
				throw new IOException("Bad line(" + line +") in (" + 
						mapPath + ")");
			}
			chunkMap.put(parts[0],parts[1]);
		}
		lines.close();
		FlatFile chunkIndex = new FlatFile(indexPath);
		return new ZiplinedBlockStringSequence(chunkIndex, chunkMap);
	}
	/**
	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinedBlockStringSequence#getIterator(java.lang.String, long)}.
	 * @throws IOException 
	 * @throws ResourceIndexNotAvailableException 
	 */
	public void testGetIteratorStringLong() throws IOException, ResourceIndexNotAvailableException {
		ZiplinedBlockStringSequence seq = getSequence();
		StringPrefixIterator itr = seq.getIterator("yahoo.com/", 1000000);
		System.out.format("Total Matches %d\n",itr.getTotalMatches());
		for(int i = 0; i < 10; i++) {
			if(itr.hasNext()) {
				System.out.format("Line(%d): %s\n",i,itr.next());
			}
		}
	}

	/**
	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinedBlockStringSequence#getIterator(java.lang.String)}.
	 */
	public void testGetIteratorString() {
//		fail("Not yet implemented");
	}

}
