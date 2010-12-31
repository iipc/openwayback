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

import java.util.Iterator;


import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ZiplinesSearchResultSourceTest extends TestCase {

//	/**
//	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinesSearchResultSource#getPrefixIterator(java.lang.String)}.
//	 * @throws CDXFormatException 
//	 */
//	public void testGetPrefixIterator() throws Exception {
//		CDXFormat format = new CDXFormat(" CDX N b a m s k r M V g");
//		ZiplinesSearchResultSource zsrs = new ZiplinesSearchResultSource(format);
////		zsrs.setChunkIndexPath("/home/brad/zipline-test/part-00005-frag.cdx.zlm");
////		zsrs.setChunkMapPath("/home/brad/zipline-test/manifest.txt");
//		zsrs.setChunkIndexPath("/home/brad/ALL.summary");
//		zsrs.setChunkMapPath("/home/brad/ALL.loc");
//		zsrs.init();
//		Iterator<String> i = zsrs.getStringPrefixIterator("krunch.com/ ");
//		int max = 100;
//		int done = 0;
//		while(i.hasNext()) {
//			System.out.println(i.next());
//			if(done++ > max) {
//				break;
//			}
//		}
//	}

	public void testEmpty() throws Exception {

	}	
}
