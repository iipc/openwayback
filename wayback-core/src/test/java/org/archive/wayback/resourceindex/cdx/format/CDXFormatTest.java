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
package org.archive.wayback.resourceindex.cdx.format;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;

import junit.framework.TestCase;

public class CDXFormatTest extends TestCase {
	public void testParseSpec() {
		CaptureSearchResult c;
		CDXFormat f = OKFormat(" CDX a V");
		c = OKParse(f,"http://foo.com 12");
		assertEquals("http://foo.com",c.getOriginalUrl());
		assertEquals(c.getOffset(), 12);

		
		f = OKFormat(" CDX a V k");
		c = OKParse(f,"http://foo.com 12 10");
		assertEquals("http://foo.com",c.getOriginalUrl());
		assertEquals(12,c.getOffset());
		assertEquals("10",c.getDigest());
		
		
		exceptionFormat("CDX a k");
		exceptionFormat("\tCDX a k");
		exceptionFormat("\tCDX a k ");
		exceptionFormat(" CDX\ta k");
		exceptionFormat(" CDX\ta k\t");
		exceptionFormat(" CDX\ta\tk\t");
		
		f = OKFormat(" CDX\ta\tV\tk");
		c = OKParse(f,"http://foo.com\t12\t10");
		assertEquals("http://foo.com",c.getOriginalUrl());
		assertEquals(12,c.getOffset());
		assertEquals("10",c.getDigest());

		c = OKParse(f,"http://foo .com\t12\t10");
		assertEquals("http://foo .com",c.getOriginalUrl());
		assertEquals(12,c.getOffset());
		assertEquals("10",c.getDigest());
	}
	private CaptureSearchResult OKParse(CDXFormat f, String line) {
		CaptureSearchResult r = null;
		try {
			r = f.parseResult(line);
		} catch (CDXFormatException e) {
			fail(e.getLocalizedMessage());
		}
		return r;
	}
	private CDXFormat OKFormat(String format) {
		CDXFormat f = null;
		try {
			f = new CDXFormat(format);
		} catch (CDXFormatException e) {
			fail("Format '" + format + "' should NOT have thrown exception");
		}
		return f;
	}
	private void exceptionFormat(String format) {
		try {
			new CDXFormat(format);
			fail("Format '" + format + "' should have thrown exception");
		} catch (CDXFormatException e) {
		}
		
	}
}
