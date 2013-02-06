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

import junit.framework.TestCase;

import org.archive.wayback.exception.ResourceIndexNotAvailableException;

/**
 * @author brad
 *
 */
public class ZiplinedBlockStringSequenceTest extends TestCase {
	private String indexPath = "/home/brad/os-cdx/CDX-201002-clean/ALL.count.summary";
	private String mapPath = "/home/brad/os-cdx/CDX-201002-clean/ALL.loc-workstation";

//	private ZiplinedBlockStringSequence getSequence() throws IOException {
//		HashMap<String, String> chunkMap = new HashMap<String, String>();
//		FlatFile ff = new FlatFile(mapPath);
//		CloseableIterator<String> lines = ff.getSequentialIterator();
//		while(lines.hasNext()) {
//			String line = lines.next();
//			String[] parts = line.split("\\s");
//			if(parts.length != 2) {
//				throw new IOException("Bad line(" + line +") in (" + 
//						mapPath + ")");
//			}
//			chunkMap.put(parts[0],parts[1]);
//		}
//		lines.close();
//		FlatFile chunkIndex = new FlatFile(indexPath);
//		return new ZiplinedBlockStringSequence(chunkIndex, chunkMap);
//	}
	/**
	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinedBlockStringSequence#getIterator(java.lang.String, long)}.
	 * @throws IOException 
	 * @throws ResourceIndexNotAvailableException 
	 */
	public void testGetIteratorStringLong() throws IOException, ResourceIndexNotAvailableException {
//		ZiplinedBlockStringSequence seq = getSequence();
//		StringPrefixIterator itr = seq.getIterator("yahoo.com/", 1000000);
//		System.out.format("Total Matches %d\n",itr.getTotalMatches());
//		for(int i = 0; i < 10; i++) {
//			if(itr.hasNext()) {
//				System.out.format("Line(%d): %s\n",i,itr.next());
//			}
//		}
	}

	/**
	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinedBlockStringSequence#getIterator(java.lang.String)}.
	 */
	public void testGetIteratorString() {
//		fail("Not yet implemented");
	}

}
