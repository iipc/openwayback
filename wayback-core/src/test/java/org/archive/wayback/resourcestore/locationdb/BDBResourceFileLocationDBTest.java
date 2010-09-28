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
import java.io.IOException;
import java.util.Iterator;

import org.archive.wayback.resourcestore.locationdb.BDBResourceFileLocationDB;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date: 2007-07-24 17:17:15 -0700 (Tue, 24 Jul 2007) $, $Revision: 1856 $
 */
public class BDBResourceFileLocationDBTest extends TestCase {
	private BDBResourceFileLocationDB db = null;
	private String dbPath = null;
	private String dbName = null;
	private File tmpFile = null;
	private File tmpLogFile = null;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		tmpFile = File.createTempFile("test-FileLocationDB",".tmp");
		tmpLogFile = File.createTempFile("test-FileLocationDB",".log");
		assertTrue(tmpFile.delete());
		assertTrue(tmpFile.mkdirs());
		dbPath = tmpFile.getAbsolutePath();
		dbName = "test-FileLocationDB";
		db = new BDBResourceFileLocationDB();
		
		db.setBdbName(dbName);
		db.setBdbPath(dbPath);
		db.setLogPath(tmpLogFile.getAbsolutePath());
		db.init();
		
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		db.shutdown();
		if(tmpFile.isDirectory()) {
			File files[] = tmpFile.listFiles();
			for(int i = 0; i < files.length; i++) {
				assertTrue(files[i].isFile());
				assertTrue(files[i].delete());
			}
			assertTrue(tmpFile.delete());
		}
		assertTrue(tmpLogFile.delete());
	}

	private void testMarkLength(long start, long end, int count) throws IOException {
		Iterator<String> itr = db.getNamesBetweenMarks(start,end);
		int found = 0;
		while(itr.hasNext()) {
			itr.next();
			found++;
		}
		assertEquals(count,found);
	}
	
	/**
	 * Test method for 'org.archive.wayback.http11resourcestore.FileLocationDB
	 */
	public void testDB() {
		assertNotNull(db);
		String urls[] = null;
		try {
			// empty results OK:
			urls = db.nameToUrls("arc1");
			assertNull(urls);
			//assertEquals(urls.length,0);
			testMarkLength(0,0,0);
			
			// add an URL, and get it back:
			db.addNameUrl("arc1","url1");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url1",urls[0]);
			testMarkLength(0,5,1);
			
			// add the same URL again, verify only comes back once:
			db.addNameUrl("arc1","url1");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url1",urls[0]);
			testMarkLength(0,5,1);

			// check for empty results for a diff arc:
			urls = db.nameToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
			// add a diff URL for first arc, verify both come back:
			db.addNameUrl("arc1","url2");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(2,urls.length);
			assertEquals("url1",urls[0]);
			assertEquals("url2",urls[1]);
			testMarkLength(0,5,1);
			
			// still nothing for arc2:
			urls = db.nameToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);

			// add an URL for arc2, and get it back:
			db.addNameUrl("arc2","url2-1");
			urls = db.nameToUrls("arc2");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2-1",urls[0]);
			testMarkLength(0,10,2);
			testMarkLength(5,10,1);

			// remove unknown URL for arc2
			db.removeNameUrl("arc2","url2-2");
			urls = db.nameToUrls("arc2");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2-1",urls[0]);
			
			// remove the right URL for arc2
			db.removeNameUrl("arc2","url2-1");
			urls = db.nameToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
			// remove non-existant URL for first arc, verify two still come back
			db.removeNameUrl("arc1","url-non");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(2,urls.length);
			assertEquals("url1",urls[0]);
			assertEquals("url2",urls[1]);
			
			// remove a right URL for arc1
			db.removeNameUrl("arc1","url1");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2",urls[0]);

			// remove a now wrong URL for arc1
			db.removeNameUrl("arc1","url1");
			urls = db.nameToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2",urls[0]);

			// remove a last URL for arc1
			db.removeNameUrl("arc1","url2");
			urls = db.nameToUrls("arc1");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
		} catch (Exception e) {
			fail("arcToUrls threw " + e.getMessage());
		}
		
	}
}
