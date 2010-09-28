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

import java.nio.ByteBuffer;

/**
 * @author brad
 *
 */
public class BitArray {
	private static final int MASKS[] = {
		0x01,
		0x02,
		0x04,
		0x08,
		0x10,
		0x20,
		0x40,
		0x80
	};
	private static final int MASKSR[] = {
		~0x01,
		~0x02,
		~0x04,
		~0x08,
		~0x10,
		~0x20,
		~0x40,
		~0x80
	};
	private ByteBuffer bb;
	
	/**
	 * Construct a new BitArray holding at least n bits
	 * @param n number of bits to hold
	 */
	public BitArray(int n) {
		int bytes = n / 8;
		int bits = n % 8;
		if(bits > 0) {
			bytes++;
		}
		bb = ByteBuffer.allocate(bytes);
	}
	/**
	 * Construct a new BitArray using argument as initial values.
	 * @param array byte array of initial values
	 */
	public BitArray(byte array[]) {
		bb = ByteBuffer.wrap(array);
	}
	/**
	 * Construct a new BitArray holding at least n bits
	 * @param bb number of bits to hold
	 */
	public BitArray(ByteBuffer bb) {
		this.bb = bb;
	}

	/**
	 * @return the byte array backing the ByteBuffer backing this bit array.
	 */
	public byte[] getBytes() {
		return bb.array();
	}

	/**
	 * @return the ByteBuffer backing this bit array.
	 */
	public ByteBuffer getByteBuffer() {
		return bb;
	}

	/**
	 * @param i index of bit to test
	 * @return true if the i'th bit is set, false otherwise
	 */
	public boolean get(int i) {
		int idx = i / 8;
		if(idx >= bb.limit()) {
			throw new IndexOutOfBoundsException();
		}
		int bit = 7 - (i % 8);
		return ((bb.get(idx) & MASKS[bit]) == MASKS[bit]);
	}
	/**
	 * set the i'th bit to 1 or 0
	 * @param i bit number to set
	 * @param value if true, the bit is set to 1, otherwise it is set to 0
	 */
	public void set(int i, boolean value) {
		int idx = i / 8;
		if(idx >= bb.limit()) {
			throw new IndexOutOfBoundsException();
		}
		int bit = 7 - (i % 8);
		if(value) {
			bb.put(idx, (byte) (bb.get(idx) | MASKS[bit]));
		} else {
			bb.put(idx,(byte) (bb.get(idx) & MASKSR[bit]));
		}
	}
}
