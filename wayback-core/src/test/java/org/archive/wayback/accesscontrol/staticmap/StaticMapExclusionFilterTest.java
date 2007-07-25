/* StaticMapExclusionFilterFactoryTest
 *
 * $Id$
 *
 * Created on 12:39:08 PM May 29, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.staticmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.ObjectFilter;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class StaticMapExclusionFilterTest extends TestCase {

	File tmpFile = null;
	StaticMapExclusionFilterFactory factory = null;

	protected void setUp() throws Exception {
		super.setUp();
		factory = new StaticMapExclusionFilterFactory();
		tmpFile = File.createTempFile("static-map", ".tmp");
//		Properties p = new Properties();
//		p.put("resourceindex.exclusionpath", tmpFile.getAbsolutePath());
//		factory.init(p);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if(tmpFile != null && tmpFile.exists()) {
			tmpFile.delete();
		}
	}

	/**
	 * @throws Exception
	 */
	public void testBaseNoPrefix() throws Exception {
		String bases[] = {"http://www.peagreenboat.com/",
							"http://peagreenboat.com/"};
//		setTmpContents(bases);
		ObjectFilter<SearchResult> filter = getFilter(bases);
		assertTrue("unmassaged",isBlocked(filter,"www.peagreenboat.com"));
		assertTrue("unmassaged",isBlocked(filter,"peagreenboat.com"));
		assertFalse("other1",isBlocked(filter,"peagreenboatt.com"));
		assertFalse("other2",isBlocked(filter,"peagreenboat.org"));
		assertFalse("other3",isBlocked(filter,"www.peagreenboat.org"));
		// there is a problem with the SURTTokenizer... deal with ports!
//		assertFalse("other4",isBlocked(filter,"www.peagreenboat.com:8080"));
		assertTrue("subpath",isBlocked(filter,"www.peagreenboat.com/foo"));
		assertTrue("emptypath",isBlocked(filter,"www.peagreenboat.com/"));
	}
	
	private boolean isBlocked(ObjectFilter<SearchResult> filter, String url) {
		SearchResult result = new SearchResult();
		result.put(WaybackConstants.RESULT_URL,url);
		int filterResult = filter.filterObject(result);
		if(filterResult == ObjectFilter.FILTER_EXCLUDE) {
			return true;
		}
		return false;
	}
	
	private ObjectFilter<SearchResult> getFilter(String lines[]) 
		throws IOException {
		
		setTmpContents(lines);
		Map<String,Object> map = factory.loadFile(tmpFile.getAbsolutePath());
		return new StaticMapExclusionFilter(map);
	}
	
	private void setTmpContents(String[] lines) throws IOException {
		if(tmpFile != null && tmpFile.exists()) {
			tmpFile.delete();
		}
//		tmpFile = File.createTempFile("range-map","tmp");
		FileWriter writer = new FileWriter(tmpFile);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<lines.length; i++) {
			sb.append(lines[i]).append("\n");
		}
		String contents = sb.toString();
		writer.write(contents);
		writer.close();
		//factory.reloadFile();
	}
}
