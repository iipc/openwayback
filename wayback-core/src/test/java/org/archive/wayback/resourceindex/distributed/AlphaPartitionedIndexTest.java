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
package org.archive.wayback.resourceindex.distributed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.httpclient.URIException;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;

import junit.framework.TestCase;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AlphaPartitionedIndexTest extends TestCase {

	private File rangeMapFile;
	private AlphaPartitionedIndex index = null;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		createRangeMapFile();
		index = new AlphaPartitionedIndex();
		index.setCheckInterval(1000);
		index.setMapPath(rangeMapFile.getAbsolutePath());
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		rangeMapFile.delete();
	}

	/**
	 * @throws Exception
	 */
	public void testFindRange() throws Exception {
		testFindRange(index,"bam.com/","b");
		testFindRange(index,"banana.com/","c");
		testFindRange(index,"banana.net/","c");
		testFindRange(index,"banana.au/","b");
		testFindRange(index,"ape.com/","a");
		testFindRange(index,"apple.com/","b");
		testFindRange(index,"aardvark.com/","a");
		testFindRange(index,"dantheman.com/","d");
		testFindRange(index,"cool.com/","c");
		testFindRange(index,"cups.com/","d");
		testFindRange(index,"zoo.com/","d");
		testFindRange(index,"207.241.2.2/","a");
		testFindRange(index,"zztop.com/","d");
	}

	/**
	 * @throws Exception
	 */
	public void testGroupBalance() throws Exception {
		WaybackRequest r = new WaybackRequest();
		r.setRequestUrl(index.canonicalize("apple.com/"));
		RangeGroup g = index.getRangeGroupForRequest(r);
		assertEquals(g.getName(),"b");
		RangeMember b1 = g.findBestMember();
		assertEquals(b1.getUrlBase(),"b1");
		b1.noteConnectionStart();
		// b1 => 1
		// b2 => 0
		RangeMember b2 = g.findBestMember();
		assertEquals(b2.getUrlBase(),"b2");
		b2.noteConnectionStart();
		// b1 => 1
		// b2 => 1
		b1.noteConnectionStart();
		// b1 => 2
		// b2 => 1
		RangeMember b2_2 = g.findBestMember();
		assertEquals(b2_2.getUrlBase(),"b2");		
		b1.noteConnectionSuccess();
		// b1 => 1
		// b2 => 1
		RangeMember b1_2 = g.findBestMember();
		assertEquals(b1_2.getUrlBase(),"b1");		
		b1.noteConnectionStart();
		// b1 => 2
		// b2 => 1
		RangeMember b2_3 = g.findBestMember();
		assertEquals(b2_3.getUrlBase(),"b2");		
		b2_3.noteConnectionStart();
		// b1 => 2
		// b2 => 2
		b1_2.noteConnectionSuccess();
		// b1 => 1
		// b2 => 2
		RangeMember b1_3 = g.findBestMember();
		assertEquals(b1_3.getUrlBase(),"b1");		
		b1_3.noteConnectionStart();
		// b1 => 2
		// b2 => 2
		RangeMember b1_4 = g.findBestMember();
		assertEquals(b1_4.getUrlBase(),"b1");		
		b1_4.noteConnectionStart();
		// b1 => 3
		// b2 => 2
		b2_3.noteConnectionSuccess();
		// b1 => 3
		// b2 => 1
		assertEquals(g.findBestMember().getUrlBase(),"b2");		
		g.findBestMember().noteConnectionStart();		
		// b1 => 3
		// b2 => 2
		assertEquals(g.findBestMember().getUrlBase(),"b2");		
		assertEquals(g.findBestMember().getUrlBase(),"b2");		
		g.findBestMember().noteConnectionStart();		
		// b1 => 3
		// b2 => 3
		assertEquals(g.findBestMember().getUrlBase(),"b1");
		b1.noteConnectionSuccess();
		// b1 => 2
		// b2 => 3
		assertEquals(g.findBestMember().getUrlBase(),"b1");
		b1.noteConnectionFailure();
		// b1 => 1-X
		// b2 => 3
		assertEquals(g.findBestMember().getUrlBase(),"b2");
		b2.noteConnectionStart();
		// b1 => 1-X
		// b2 => 4
		assertEquals(g.findBestMember().getUrlBase(),"b2");
		b2.noteConnectionStart();
		// b1 => 1-X
		// b2 => 5
		
		// HACKHACK: how to sleep for 1 ms?
		long one = System.currentTimeMillis();
		int two = 0;
		while(System.currentTimeMillis() <= one) {
			two++;
		}
		
		b1.noteConnectionSuccess();
		// b1 => 0
		// b2 => 5
		assertEquals(g.findBestMember().getUrlBase(),"b1");
		b1.noteConnectionStart();
		// b1 => 1
		// b2 => 5
		b1.noteConnectionStart();
		b1.noteConnectionStart();
		b1.noteConnectionStart();
		b1.noteConnectionStart();
		b1.noteConnectionStart();
		// b1 => 6
		// b2 => 5
		assertEquals(g.findBestMember().getUrlBase(),"b2");
		b2.noteConnectionStart();
		// b1 => 6
		// b2 => 6
		assertEquals(g.findBestMember().getUrlBase(),"b1");
	}

	private void testFindRange(final AlphaPartitionedIndex apIndex,
			final String url, final String wantGroup) throws URIException,
			BadQueryException, ResourceIndexNotAvailableException {
		WaybackRequest r = new WaybackRequest();
		r.setRequestUrl(apIndex.canonicalize(url));
		RangeGroup g = apIndex.getRangeGroupForRequest(r);
		assertEquals(g.getName(),wantGroup);		
	}

	private void createRangeMapFile() throws IOException {
		rangeMapFile = File.createTempFile("range-map","tmp");
		FileWriter writer = new FileWriter(rangeMapFile);
		StringBuilder sb = new StringBuilder();
		sb.append("d cups.com/ zorro.com/ d1 d2\n");
		sb.append("b apple.com/ banana.com/ b1 b2\n");
		sb.append("a  apple.com/ a1 a2\n");
		sb.append("c banana.com/ cups.com/ c1 c2\n");
		writer.write(sb.toString());
		writer.close();
	}
}
