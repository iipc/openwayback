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
import java.io.OutputStreamWriter;
import java.io.Writer;

import junit.framework.TestCase;

import org.archive.io.warc.TestWARCReader;
import org.archive.io.warc.TestWARCRecordInfo;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.wayback.core.Resource;
import org.archive.wayback.resourcestore.resourcefile.WarcResource;

/**
 * Test for GzipDecodingResource.
 */
public class GzipDecodingResourceTest extends TestCase {

	public void testRegularGzipped() throws Exception {
        final byte[] payload = "ABCDEFG".getBytes("UTF-8");
        String ctype = "text/plain";
		WARCRecordInfo recinfo = new TestWARCRecordInfo(
			TestWARCRecordInfo.buildCompressedHttpResponseBlock(ctype, payload));
        TestWARCReader ar = new TestWARCReader(recinfo);
        WARCRecord rec = (WARCRecord)ar.get(0);
        Resource resource = new WarcResource(rec, ar);
        resource.parseHeaders();

		GzipDecodingResource cut = new GzipDecodingResource(resource);

		byte[] content = new byte[payload.length];
		int n = cut.read(content);
		cut.close();

		// If GzipDecodingResource does not reset the input stream,
		// we'll get 5, instead of expected 7.
		assertEquals(payload.length, n);
		assertEquals('A', content[0]);
		assertEquals('B', content[1]);
	}
	/**
	 * Sometimes response lies about content-encoding.
	 * If content is not actually gzip-compressed, GzipDecodingResource
	 * shall fall back on reading content as it is.
	 * @throws Exception
	 */
	public void testNotGzipped() throws Exception {
		// create HTTP response with false Content-Encoding
		final byte[] payload = "ABCDEFG".getBytes("UTF-8");
		ByteArrayOutputStream blockbuf = new ByteArrayOutputStream();
		Writer w = new OutputStreamWriter(blockbuf);
		w.write("HTTP/1.0 200 OK\r\n");
		w.write("Content-Length: " + payload.length + "\r\n");
		w.write("Content-Encoding: gzip\r\n");
		w.write("\r\n");
		w.flush();
		blockbuf.write(payload);
		w.close();
		TestWARCRecordInfo recinfo = new TestWARCRecordInfo(blockbuf.toByteArray());

		TestWARCReader ar = new TestWARCReader(recinfo);
		WARCRecord rec = (WARCRecord)ar.get(0);
		Resource resource = new WarcResource(rec, ar);
		resource.parseHeaders();

		GzipDecodingResource cut = new GzipDecodingResource(resource);

		byte[] content = new byte[payload.length];
		int n = cut.read(content);
		cut.close();

		// If GzipDecodingResource does not reset the input stream,
		// we'll get 5, instead of expected 7.
		assertEquals(payload.length, n);
		assertEquals('A', content[0]);
		assertEquals('B', content[1]);
	}
}
