/* ByteOp
 *
 * $Id$
 *
 * Created on 3:56:12 PM Dec 16, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * SocksProxyCore is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * SocksProxyCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with SocksProxyCore; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Byte oriented static methods. Likely a lot of overlap with apache- commons
 * stuff - eventually should be reconciled.
 * 
 * @author brad
 *
 */
public class ByteOp {
	/** Default buffer size for IO ops */
	public final static int BUFFER_SIZE = 4096;
	
	/**
	 * Create a new byte array with contents initialized to values from the
	 * argument byte array.
	 * @param src source byte array of initial values
	 * @param offset start offset to copy bytes
	 * @param length number of bytes to copy
	 * @return a new byte array of size length, containing values from src 
	 * starting from offset in the src array.
	 */
	public static byte[] copy(byte[] src, int offset, int length) {
		byte[] copy = new byte[length];
		System.arraycopy(src, offset, copy, 0, length);
		return copy;
	}

	/**
	 * Compare two byte arrays
	 * @param a byte array to compare
	 * @param b byte array to compare
	 * @return true if a and b have same length, and all the same values, false
	 * otherwise
	 */
	public static boolean cmp(byte[] a, byte[] b) {
		if(a.length != b.length) {
			return false;
		}
		for(int i = 0; i < a.length; i++) {
			if(a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * throw away all bytes from stream argument
	 * @param is InputStream to read and discard
	 * @throws IOException when is throws one
	 */
	public static void discardStream(InputStream is) throws IOException {
		discardStream(is,BUFFER_SIZE);
	}

	/**
	 * throw away all bytes from stream argument
	 * @param is InputStream to read and discard
	 * @param size number of bytes to read at once from the stream
	 * @throws IOException when is throws one
	 */
	public static void discardStream(InputStream is,int size) throws IOException {
		byte[] buffer = new byte[size];
		while(is.read(buffer, 0, size) != -1) {
		}
	}

	/**
	 * throw away all bytes from stream argument, and count how many bytes were
	 * discarded before reaching the end of the stream.
	 * @param is InputStream to read and discard
	 * @return the number of bytes discarded
	 * @throws IOException when is throws one
	 */
	public static long discardStreamCount(InputStream is) throws IOException {
		return discardStreamCount(is, BUFFER_SIZE);
	}

	/**
	 * throw away all bytes from stream argument, and count how many bytes were
	 * discarded before reaching the end of the stream.
	 * @param is InputStream to read and discard
	 * @param size number of bytes to read at once from the stream
	 * @return the number of bytes discarded
	 * @throws IOException when is throws one
	 */
	public static long discardStreamCount(InputStream is,int size) throws IOException {
		long count = 0;
		byte[] buffer = new byte[size];
		int amt = 0;
		while((amt = is.read(buffer, 0, size)) != -1) {
			count += amt;
		}
		return count;
	}
	
	/**
	 * Write all bytes from is to os. Does not close either stream.
	 * @param is to copy bytes from
	 * @param os to copy bytes to
	 * @throws IOException for usual reasons
	 */
	public static void copyStream(InputStream is, OutputStream os) 
	throws IOException {
		copyStream(is,os,BUFFER_SIZE);
	}

	/**
	 * Write all bytes from is to os. Does not close either stream.
	 * @param is to copy bytes from
	 * @param os to copy bytes to
	 * @param size number of bytes to buffer between read and write operations
	 * @throws IOException for usual reasons
	 */
	public static void copyStream(InputStream is, OutputStream os, int size) 
	throws IOException {
		byte[] buffer = new byte[size];
		for (int r = -1; (r = is.read(buffer, 0, size)) != -1;) {
			os.write(buffer, 0, r);
		}
	}
}
