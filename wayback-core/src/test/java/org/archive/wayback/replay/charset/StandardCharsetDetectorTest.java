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

package org.archive.wayback.replay.charset;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

/**
 *
 */
public class StandardCharsetDetectorTest extends TestCase {

	StandardCharsetDetector cut;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cut = new StandardCharsetDetector();
	}

	public static Resource createTestResource(String ctype,
			byte[] payloadBytes, boolean compressed) throws IOException {
		WARCRecordInfo recinfo = compressed ? TestWARCRecordInfo
			.createCompressedHttpResponse(ctype, payloadBytes)
				: TestWARCRecordInfo.createHttpResponse(ctype, payloadBytes);
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = ar.get(0);
		WarcResource resource = new WarcResource(rec, ar);
		resource.parseHeaders();
		return resource;
	}

	public void testGetCharset_1() throws Exception {
		// juniversalchardet fails to detect this as windows-1252.
		// it has HTML5 <meta charset="utf-8" />
		InputStream is = getClass().getResourceAsStream("french-utf-8.html");
		byte[] payloadBytes = IOUtils.toByteArray(is);
		Resource resource = createTestResource("text/html", payloadBytes, false);

		WaybackRequest request = WaybackRequest.createReplayRequest(
			"http://example.com/", "201501010123030", null, null);

		String charset = cut.getCharset(resource, request);
		if (charset != null)
			charset = charset.toUpperCase();
		assertEquals("UTF-8", charset);
	}

}
