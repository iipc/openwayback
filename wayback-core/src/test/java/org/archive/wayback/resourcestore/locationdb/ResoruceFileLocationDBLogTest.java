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
package org.archive.wayback.resourcestore.locationdb;

import java.io.File;
import java.util.Iterator;

import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDBLog;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date: 2007-07-24 17:17:15 -0700 (Tue, 24 Jul 2007) $, $Revision: 1856 $
 */
public class ResoruceFileLocationDBLogTest extends TestCase {
	ResourceFileLocationDBLog log;
    protected void setUp() throws Exception {
        super.setUp();
		File tmp = File.createTempFile("fldb","log");

		log = new ResourceFileLocationDBLog(tmp.getAbsolutePath());        
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
		Iterator<String> itr = log.getNamesBetweenMarks(0,0);
		assertFalse(itr.hasNext());
		log.addName(newArc1);
		long mark2 = log.getCurrentMark();
		assertEquals(newArc1.length() + 1,mark2);
		itr = log.getNamesBetweenMarks(mark1,mark2);
		assertTrue(itr.hasNext());
		String gotArc = (String) itr.next();
		assertFalse(itr.hasNext());
		assertTrue(newArc1.equals(gotArc));
		log.addName(newArc2);
		long mark3 = log.getCurrentMark();
		assertEquals(newArc1.length() + newArc2.length() + 2, mark3);

		itr = log.getNamesBetweenMarks(mark2,mark3);
		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertFalse(itr.hasNext());
		assertTrue(newArc2.equals(gotArc));

		itr = log.getNamesBetweenMarks(mark1,mark3);
		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertTrue(newArc1.equals(gotArc));

		assertTrue(itr.hasNext());
		gotArc = (String) itr.next();
		assertTrue(newArc2.equals(gotArc));
		
		assertFalse(itr.hasNext());
	}
}
