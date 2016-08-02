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

package org.archive.wayback.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

/**
 *
 */
public class InflatingResourceTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected byte[] deflateContent(String content, boolean noHeader) throws IOException {
		ByteArrayOutputStream deflated = new ByteArrayOutputStream();
		DeflaterOutputStream deflator = new DeflaterOutputStream(deflated,
			new Deflater(5, noHeader));
		deflator.write(content.getBytes("UTF-8"));
		deflator.close();
		return deflated.toByteArray();
	}

	protected Resource testResource(String content, boolean noHeader) throws IOException {
		final byte[] payload = deflateContent(content, false);
		ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(blockbuf);
		w.write("HTTP/1.0 200 OK\r\n");
		w.write("Content-Length: " + payload.length + "\r\n");
		w.write("Content-Encoding: deflate\r\n");
		w.write("\r\n");
		w.flush();
		blockbuf.write(payload);
		TestWARCRecordInfo recinfo = new TestWARCRecordInfo(blockbuf.toByteArray());
		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = (WARCRecord)ar.get(0);
		Resource resource = new WarcResource(rec, ar);
		resource.parseHeaders();
		return resource;
	}

	public void testStandardDeflatedContent() throws Exception {
		Resource resource = testResource("ABCDEFG", false);
		InflatingResource wrapped = new InflatingResource(resource);

		byte[] content = new byte[80];
		int n = wrapped.read(content);
		wrapped.close();
		String text = new String(content, 0, n, "UTF-8");
		assertEquals("ABCDEFG", text);
	}

	public void testHeaderlessDeflatedContent() throws Exception {
		Resource resource = testResource("ABCDEFG", true);
		InflatingResource wrapped = new InflatingResource(resource);

		byte[] content = new byte[80];
		int n = wrapped.read(content);
		wrapped.close();
		String text = new String(content, 0, n, "UTF-8");
		assertEquals("ABCDEFG", text);
	}

}
