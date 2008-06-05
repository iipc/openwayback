/* FileLocationDBTest
 *
 * $Id: FileLocationDBTest.java 1856 2007-07-25 00:17:15Z bradtofel $
 *
 * Created on 5:17:23 PM Aug 21, 2006.
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
