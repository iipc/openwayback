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
package org.archive.wayback.util;

import junit.framework.TestCase;

/**
 * @author brad
 *
 */
public class BitArrayTest extends TestCase {

	/**
	 * Test method for {@link org.archive.wayback.util.BitArray#get(int)}.
	 */
	public void testGet() {
		byte bytes[] = "Here is some data!".getBytes();
		byte bytes2[] = "Here is some data!".getBytes();
		int bits = bytes.length * 8;
		sun.security.util.BitArray sba = 
			new sun.security.util.BitArray(bits, bytes);
		org.archive.wayback.util.BitArray wba = 
			new org.archive.wayback.util.BitArray(bytes2);
		for(int i = 0; i < bits; i++) {
			boolean want = sba.get(i);
			boolean got = wba.get(i);
			if(want != got) {
				got = wba.get(i);
			}
			assertEquals(want,got);
		}
	}

	/**
	 * Test method for {@link org.archive.wayback.util.BitArray#set(int, boolean)}.
	 */
	public void testSet() {
		byte bytes[] = "Here is some data!".getBytes();
		byte bytes2[] = "Here is some data!".getBytes();
		int bits = bytes.length * 8;
		sun.security.util.BitArray sba = 
			new sun.security.util.BitArray(bits, bytes);
		org.archive.wayback.util.BitArray wba = 
			new org.archive.wayback.util.BitArray(bytes2);
		for(int i = 0; i < bits; i++) {
			boolean want = sba.get(i);
			boolean got = wba.get(i);
			boolean not = !want;
			assertTrue(ByteOp.cmp(sba.toByteArray(), wba.getBytes()));
			sba.set(i,not);
			wba.set(i,not);
			assertTrue(ByteOp.cmp(sba.toByteArray(), wba.getBytes()));
			assertEquals(not,wba.get(i));
			sba.set(i,got);
			wba.set(i,got);
			assertEquals(sba.get(i),wba.get(i));			
			assertTrue(ByteOp.cmp(sba.toByteArray(), wba.getBytes()));
		}
		
	}
}
