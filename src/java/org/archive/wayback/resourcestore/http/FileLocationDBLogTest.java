/* FileLocationDBLogTest
 *
 * $Id$
 *
 * Created on 4:54:04 PM Aug 21, 2006.
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
package org.archive.wayback.resourcestore.http;

import java.io.File;
import java.util.Iterator;
import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDBLogTest extends TestCase {
	FileLocationDBLog log;
    protected void setUp() throws Exception {
        super.setUp();
		File tmp = File.createTempFile("fldb","log");

		log = new FileLocationDBLog(tmp.getAbsolutePath());        
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
		if(!log.delete()) {
			fail("FAILED to delete tmp file");
		}
    }

	/**
	 * @throws Exception
	 */
	public void testEmptyFile() throws Exception {
		String newArc1 = "foo.arc.gz";
		String newArc2 = "bar.arc.gz";
		long mark1 = log.getCurrentMark();
		assertEquals(mark1,0);
		Iterator itr = log.getArcsBetweenMarks(0,0);
		assertFalse(itr.hasNext());
		log.addArc(newArc1);
		long mark2 = log.getCurrentMark();
		assertEquals(newArc1.length() + 1,mark2);
		itr = log.getArcsBetweenMarks(mark1,mark2);
		assertTrue(itr.hasNext());
		String gotArc = (String) itr.next();
		assertFalse(itr.hasNext());
		assertTrue(newArc1.equals(gotArc));
		log.addArc(newArc2);
		long mark3 = log.getCurrentMark();
		assertEquals(newArc1.length() + newArc2.length() + 2, mark3);

		itr = log.getArcsBetweenMarks(mark2,mark3);
		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertFalse(itr.hasNext());
		assertTrue(newArc2.equals(gotArc));

		itr = log.getArcsBetweenMarks(mark1,mark3);
		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertTrue(newArc1.equals(gotArc));

		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertTrue(newArc2.equals(gotArc));
		
		assertFalse(itr.hasNext());
	}
}
