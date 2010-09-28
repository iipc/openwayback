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
package org.archive.wayback.resourceindex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;
import org.w3c.dom.Document;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class RemoteResourceIndexTest extends TestCase {
	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	/**
	 * Test method for {@link org.archive.wayback.resourceindex.RemoteResourceIndex#documentToSearchResults(org.w3c.dom.Document, org.archive.wayback.util.ObjectFilter)}.
	 */
	public void testDocumentToSearchResults() {
		RemoteResourceIndex r = new RemoteResourceIndex();
		DocumentBuilder db;
		try {
			db = factory.newDocumentBuilder();
			
			String testXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<wayback>"
				+ "<request>"
				+ "<startdate>20090101000000</startdate>"
				+ "<numreturned>1</numreturned>"
				+ "<type>urlquery</type>"
				+ "<enddate>20100810191351</enddate>"
				+ "<numresults>1</numresults>"
				+ "<firstreturned>0</firstreturned>"
				+ "<url>dixienet.org/</url>"
				+ "<resultsrequested>1000</resultsrequested>"
				+ "<resultstype>resultstypecapture</resultstype>"
				+ "</request>"
				+ "<results>"
				+ "<result>"
				+ "<compressedoffset>36717460</compressedoffset>"
				+ "<mimetype>text/html</mimetype>"
				+ "<file>LOC-TRANSITION-001-20090204213039-00004-crawling108.us.archive.org.warc.gz</file>"
				+ "<redirecturl>http://dixienet.org/New%20Site/index.shtml</redirecturl>"
				+ "<urlkey>dixienet.org/</urlkey>"
				+ "<digest>3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ</digest>"
				+ "<httpresponsecode>302</httpresponsecode>"
				+ "<url>http://dixienet.org/</url>"
				+ "<capturedate>20090204213443</capturedate>"
				+ "</result>"
				+ "</results>"
				+ "</wayback>";
			ByteArrayInputStream bais = new ByteArrayInputStream(testXML.getBytes());
			Document document = db.parse(bais);
			
			ObjectFilterChain<CaptureSearchResult> filter = new ObjectFilterChain<CaptureSearchResult>();
			filter.addFilter(new ObjectFilter<CaptureSearchResult>() {
				
				public int filterObject(CaptureSearchResult o) {
					return ObjectFilter.FILTER_INCLUDE;
				}
			});
			CaptureSearchResults sr = (CaptureSearchResults) r.documentToSearchResults(document, filter);
			assertEquals(1,sr.getResults().size());
			assertEquals("20090204213443",sr.getResults().get(0).getCaptureTimestamp());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

}
