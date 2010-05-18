/* BitArrayTest
 *
 * $Id$:
 *
 * Created on May 14, 2010.
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
