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
package org.archive.wayback.accesscontrol.staticmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

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
	UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	protected void setUp() throws Exception {
		super.setUp();
		factory = new StaticMapExclusionFilterFactory();
		tmpFile = File.createTempFile("static-map", ".tmp");
//		Properties p = new Properties();
//		p.put("resourceindex.exclusionpath", tmpFile.getAbsolutePath());
//		factory.init(p);

		// disable INFO-level logging from StaticMapExclusionFilter, not useful for
		// automated test.
		Logger.getLogger(StaticMapExclusionFilter.class.getName()).setLevel(Level.WARNING);
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
	public void testRealWorld() throws Exception {
		String bases[] = { "pho-c.co.jp/~clever",
							"sf.net/pop/Roger",
							"www.eva-stu.vn",
							"mins.com.br/",
							"24.ne.jp",
							"24.ne.jp/~nekko"};
//		setTmpContents(bases);
		
		
		ObjectFilter<CaptureSearchResult> filter = getFilter(bases);
		assertFalse("unmassaged",isBlocked(filter,"24.ne.jp.idpnt.com/robots.txt"));
		assertTrue("massage",isBlocked(filter,"http://24.ne.jp:80/"));
		assertTrue("unmassaged",isBlocked(filter,"http://www.pho-c.co.jp/~clever"));
		assertTrue("massage",isBlocked(filter,"http://24.ne.jp"));

		
		assertTrue("unmassaged",isBlocked(filter,"http://www.pho-c.co.jp/~clever"));
		assertTrue("massaged",isBlocked(filter,"http://pho-c.co.jp/~clever"));
		assertTrue("trailing-slash",isBlocked(filter,"http://pho-c.co.jp/~clever/"));
		assertTrue("subpath",isBlocked(filter,"http://pho-c.co.jp/~clever/foo.txt"));

		assertTrue("full-port",isBlocked(filter,"http://www.mins.com.br:80"));
		assertTrue("tail-slash-port",isBlocked(filter,"http://www.mins.com.br:80/"));
		assertTrue("full",isBlocked(filter,"http://www.mins.com.br"));
		assertTrue("tail-slash",isBlocked(filter,"http://www.mins.com.br/"));
		assertTrue("full-massage",isBlocked(filter,"http://mins.com.br"));
		assertTrue("tail-slash-massage",isBlocked(filter,"http://mins.com.br/"));
		assertTrue("massage",isBlocked(filter,"http://mins.com.br/foo.txt"));
		assertTrue("subpath",isBlocked(filter,"http://www13.mins.com.br/~clever/foo.txt"));

		assertTrue("massage",isBlocked(filter,"24.ne.jp"));
		assertTrue("full",isBlocked(filter,"http://www.mins.com.br"));
		assertTrue("subpath",isBlocked(filter,"www.24.ne.jp"));
		assertTrue("tail-slash-massage",isBlocked(filter,"http://mins.com.br/"));
		assertTrue("subpath",isBlocked(filter,"http://www.24.ne.jp:80/"));
		

		
		
		assertTrue(isBlocked(filter,"http://sf.net/pop/Roger"));
		assertTrue(isBlocked(filter,"http://sf.net/pop/Roger/"));
		assertTrue(isBlocked(filter,"http://sf.net/pop/Roger//"));
		assertFalse(isBlocked(filter,"http://sf.net/pop/"));
		assertTrue(isBlocked(filter,"http://sf.net/pop/Roger/2"));
		assertTrue(isBlocked(filter,"http://sf.net/pop/Roger/23"));
		assertTrue(isBlocked(filter,"http://www.sf.net/pop/Roger"));
		assertTrue(isBlocked(filter,"http://www1.sf.net/pop/Roger"));
		assertTrue(isBlocked(filter,"http://www23.sf.net/pop/Roger"));

		assertTrue(isBlocked(filter,"http://www23.eva-stu.vn/"));
		assertTrue(isBlocked(filter,"http://www23.eva-stu.vn"));
		assertTrue(isBlocked(filter,"http://eva-stu.vn"));
		assertTrue(isBlocked(filter,"http://www.eva-stu.vn/"));
		assertTrue(isBlocked(filter,"http://eva-stu.vn/"));
		assertTrue(isBlocked(filter,"http://www.eva-stu.vn/foo.txt"));
		assertTrue(isBlocked(filter,"http://www2.eva-stu.vn/foo/bar.txt"));
		assertTrue(isBlocked(filter,"http://eva-stu.vn/foo/bar.txt"));

	}

	
	/**
	 * @throws Exception
	 */
	public void testBaseNoPrefix() throws Exception {
		String bases[] = {"http://www.peagreenboat.com/",
							"http://peagreenboat.com/"};
//		setTmpContents(bases);
		ObjectFilter<CaptureSearchResult> filter = getFilter(bases);
		assertTrue("unmassaged",isBlocked(filter,"http://www.peagreenboat.com"));
		assertTrue("unmassaged",isBlocked(filter,"http://peagreenboat.com"));
		assertFalse("other1",isBlocked(filter,"http://peagreenboatt.com"));
		assertFalse("other2",isBlocked(filter,"http://peagreenboat.org"));
		assertFalse("other3",isBlocked(filter,"http://www.peagreenboat.org"));
		// there is a problem with the SURTTokenizer... deal with ports!
//		assertFalse("other4",isBlocked(filter,"http://www.peagreenboat.com:8080"));
		assertTrue("subpath",isBlocked(filter,"http://www.peagreenboat.com/foo"));
		assertTrue("emptypath",isBlocked(filter,"http://www.peagreenboat.com/"));
	}
	
	private boolean isBlocked(ObjectFilter<CaptureSearchResult> filter, String url) throws URIException {
		CaptureSearchResult result = new CaptureSearchResult();
		result.setOriginalUrl(url);
		result.setUrlKey(canonicalizer.urlStringToKey(url));
		int filterResult = filter.filterObject(result);
		if(filterResult == ObjectFilter.FILTER_EXCLUDE) {
			return true;
		}
		return false;
	}
	
	private ObjectFilter<CaptureSearchResult> getFilter(String lines[]) 
		throws IOException {
		
		setTmpContents(lines);
		Map<String,Object> map = factory.loadFile(tmpFile.getAbsolutePath());
		return new StaticMapExclusionFilter(map,canonicalizer);
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
