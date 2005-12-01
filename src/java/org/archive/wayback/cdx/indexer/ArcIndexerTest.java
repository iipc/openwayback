/* ArcIndexerTest
 *
 * $Id$
 *
 * Created on 4:56:14 PM Nov 18, 2005.
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
package org.archive.wayback.cdx.indexer;

import java.io.File;
import java.io.IOException;

import org.archive.wayback.core.SearchResults;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcIndexerTest extends TestCase {
	/**
	 * temporary File
	 */
	File tmpFile = null;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		tmpFile = File.createTempFile("results",".tmp");
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(tmpFile != null && tmpFile.isFile()) {
			tmpFile.delete();
		}
	}

	/**
	 * Test method for 'org.archive.wayback.cdx.indexer.ArcIndexer.serializeResults(SearchResults, File)'
	 */
	public void testSerializeResults() { 
		ArcIndexer indexer = new ArcIndexer();
		SearchResults results = new SearchResults();
		assertNotNull(indexer);
		assertNotNull(results);
		
		try {
			indexer.serializeResults(results,tmpFile);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
