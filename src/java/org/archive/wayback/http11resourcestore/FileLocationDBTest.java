/* FileLocationDBTest
 *
 * $Id$
 *
 * Created on 4:29:05 PM Dec 14, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.http11resourcestore;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDBTest extends TestCase {

	private FileLocationDB db = null;
	private String dbPath = null;
	private String dbName = null;
	private File tmpFile = null;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {

		tmpFile = File.createTempFile("test-FileLocationDB",".tmp");
		assertTrue(tmpFile.delete());
		assertTrue(tmpFile.mkdirs());
		dbPath = tmpFile.getAbsolutePath();
		dbName = "test-FileLocationDB";
		db = new FileLocationDB();
		db.initializeDB(dbPath,dbName);
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		db.shutdownDB();
		if(tmpFile.isDirectory()) {
			File files[] = tmpFile.listFiles();
			for(int i = 0; i < files.length; i++) {
				assertTrue(files[i].isFile());
				assertTrue(files[i].delete());
			}
			assertTrue(tmpFile.delete());
		}
	}

	/**
	 * Test method for 'org.archive.wayback.http11resourcestore.FileLocationDB
	 */
	public void testDB() {
		assertNotNull(db);
		String urls[] = null;
		try {
			// empty results OK:
			urls = db.arcToUrls("arc1");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
			// add an URL, and get it back:
			db.addArcUrl("arc1","url1");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url1",urls[0]);

			// add the same URL again, verify only comes back once:
			db.addArcUrl("arc1","url1");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url1",urls[0]);

			// check for empty results for a diff arc:
			urls = db.arcToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
			// add a diff URL for first arc, verify both come back:
			db.addArcUrl("arc1","url2");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(2,urls.length);
			assertEquals("url1",urls[0]);
			assertEquals("url2",urls[1]);
			
			// still nothing for arc2:
			urls = db.arcToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);

			// add an URL for arc2, and get it back:
			db.addArcUrl("arc2","url2-1");
			urls = db.arcToUrls("arc2");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2-1",urls[0]);

			// remove unknown URL for arc2
			db.removeArcUrl("arc2","url2-2");
			urls = db.arcToUrls("arc2");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2-1",urls[0]);
			
			// remove the right URL for arc2
			db.removeArcUrl("arc2","url2-1");
			urls = db.arcToUrls("arc2");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
			// remove non-existant URL for first arc, verify two still come back
			db.removeArcUrl("arc1","url-non");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(2,urls.length);
			assertEquals("url1",urls[0]);
			assertEquals("url2",urls[1]);
			
			// remove a right URL for arc1
			db.removeArcUrl("arc1","url1");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2",urls[0]);

			// remove a now wrong URL for arc1
			db.removeArcUrl("arc1","url1");
			urls = db.arcToUrls("arc1");
			assertNotNull(urls);
			assertEquals(1,urls.length);
			assertEquals("url2",urls[0]);

			// remove a last URL for arc1
			db.removeArcUrl("arc1","url2");
			urls = db.arcToUrls("arc1");
			assertNull(urls);
			//assertEquals(urls.length,0);
			
		} catch (DatabaseException e) {
			fail("arcToUrls threw " + e.getMessage());
		}
		
	}
}
