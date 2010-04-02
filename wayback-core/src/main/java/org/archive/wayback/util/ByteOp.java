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

public class ByteOp {
	public final static int BUFFER_SIZE = 4096;
	
	public static byte[] copy(byte[] src, int offset, int length) {
		byte[] copy = new byte[length];
		System.arraycopy(src, offset, copy, 0, length);
		return copy;
	}
	public static boolean cmp(byte[] input, byte[] want) {
		if(input.length != want.length) {
			return false;
		}
		for(int i = 0; i < input.length; i++) {
			if(input[i] != want[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static void discardStream(InputStream is) throws IOException {
		discardStream(is,BUFFER_SIZE);
	}
	public static void discardStream(InputStream is,int size) throws IOException {
		byte[] buffer = new byte[size];
		while(is.read(buffer, 0, size) != -1) {
		}
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
