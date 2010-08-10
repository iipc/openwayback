/* RemoteResourceIndexTest
 *
 * $Id$:
 *
 * Created on Aug 10, 2010.
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
