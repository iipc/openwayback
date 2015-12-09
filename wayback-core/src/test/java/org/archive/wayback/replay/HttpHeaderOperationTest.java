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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test for {@link HttpHeaderOperation}
 */
public class HttpHeaderOperationTest extends TestCase {

	protected String toString(long[] a) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < a.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(a[i]);
		}
		sb.append("]");
		return sb.toString();
	}

	protected boolean equals(long[] a, long[] b) {
		if (a.length != b.length) return false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) return false;
		}
		return true;
	}

	protected void assertEquals(String message, long[] expected, long[] actual) {
		if (!equals(expected, actual)) {
			fail(message + " expected " + toString(expected) + ", got " + toString(actual));
		}
	}

	protected void assertEquals(long[][] expected, long[][] actual) {
		for (int n = 0; ; n++) {
			if (n >= expected.length && n >= actual.length)
				break;
			if (n >= expected.length)
				fail("actual has more elements than expected");
			if (n >= actual.length)
				fail("actual has less elements than expected");
			assertEquals("element " + n, expected[n], actual[n]);
		}
	}

	public void testGetRange() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();

		long[][] r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=0-499");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 0, 500 } }, r);

		headers.put("Range", "bytes=500-999");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 500, 1000 } }, r);

		headers.put("Range", "bytes=0-0");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 0, 1 } }, r);
		headers.put("Range", "bytes=0-");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 0, -1 } }, r);

		headers.put("Range", "bytes=-500");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { -500, -1 } }, r);

		headers.put("Range", "bytes=0-499,500-999");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 0, 500 }, { 500, 1000 } }, r);

		headers.put("Range", "bytes=0-0,-1");
		r = HttpHeaderOperation.getRange(headers);
		assertEquals(new long[][] { { 0, 1 }, { -1, -1 } }, r);
	}

	public void testGetRange_pathological() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();

		long[][] r;
		headers.put("Range", "bytes=100");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=300-100");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=-0");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=-");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=--100-");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range", "bytes=0-ab");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);

		headers.put("Range",  "100-200");
		r = HttpHeaderOperation.getRange(headers);
		assertNull(r);
	}

	public void testGetContentRange() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();

		long[] r = HttpHeaderOperation.getContentRange(headers);
		assertNull(r);

		headers.put("Content-Range", "bytes 0-499/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertEquals("getContentRange", new long[] { 0, 500, 5000 }, r);

		headers.put("Content-Range", "bytes */5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertEquals("getContentRange", new long[] { -1, -1, 5000 }, r);

		headers.put("Content-Range", "bytes 0-499/*");
		r = HttpHeaderOperation.getContentRange(headers);
		assertEquals("getContentRange", new long[] { 0, 500, -1 }, r);

		headers.put("Content-Range", "bytes 499-499/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertEquals("getContentRange", new long[] { 499, 500, 5000 }, r);

		// ok to have multiple spaces
		headers.put("Content-Range", "bytes     0-499/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertEquals("getContentRange", new long[] { 0, 500, 5000 }, r);
	}

	public void testGetContentRange_pathological() throws Exception {
		Map<String, String> headers = new HashMap<String, String>();

		long[] r;

		headers.put("Content-Range", "bytes -499/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertNull(r);

		headers.put("Content-Range", "bytes 499-/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertNull(r);

		headers.put("Content-Range", "bytes300-299/5000");
		r = HttpHeaderOperation.getContentRange(headers);
		assertNull(r);
	}
}
