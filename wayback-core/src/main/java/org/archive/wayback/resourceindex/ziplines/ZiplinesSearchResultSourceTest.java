/* ZiplinesSearchResultSourceTest
 *
 * $Id$:
 *
 * Created on Nov 23, 2009.
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

import java.util.Iterator;

import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class ZiplinesSearchResultSourceTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.resourceindex.ziplines.ZiplinesSearchResultSource#getPrefixIterator(java.lang.String)}.
	 * @throws CDXFormatException 
	 */
	public void testGetPrefixIterator() throws Exception {
		CDXFormat format = new CDXFormat(" CDX N b a m s k r M V g");
		ZiplinesSearchResultSource zsrs = new ZiplinesSearchResultSource(format);
//		zsrs.setChunkIndexPath("/home/brad/zipline-test/part-00005-frag.cdx.zlm");
//		zsrs.setChunkMapPath("/home/brad/zipline-test/manifest.txt");
		zsrs.setChunkIndexPath("/home/brad/ALL.summary");
		zsrs.setChunkMapPath("/home/brad/ALL.loc");
		zsrs.init();
		Iterator<String> i = zsrs.getStringPrefixIterator("krunch.com/ ");
		int max = 100;
		int done = 0;
		while(i.hasNext()) {
			System.out.println(i.next());
			if(done++ > max) {
				break;
			}
		}
	}

}
