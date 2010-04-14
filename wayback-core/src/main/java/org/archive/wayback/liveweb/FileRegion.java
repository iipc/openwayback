/* FileRegion
 *
 * $Id$:
 *
 * Created on Dec 8, 2009.
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

package org.archive.wayback.liveweb;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * @author brad
 *
 */
public class FileRegion {
	File file = null;
	long start = -1;
	long end = -1;
	boolean isFake = false;
	/**
	 * @return the number of bytes in this record, including headers. If the
	 * containing file is compressed, then this represents the number of 
	 * compressed bytes.
	 */
	public long getLength() {
		return end - start;
	}
	/**
	 * Copy this record to the provided OutputStream
	 * @param o the OutputStream where the bytes should be sent.
	 * @throws IOException for usual reasons
	 */
	public void copyToOutputStream(OutputStream o) throws IOException {
		long left = end - start;
		int BUFF_SIZE = 4096;
		byte buf[] = new byte[BUFF_SIZE];
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(start);
		while(left > 0) {
			int amtToRead = (int) Math.min(left, BUFF_SIZE);
			int amtRead = raf.read(buf, 0, amtToRead);
			if(amtRead < 0) {
				throw new IOException("Not enough to read! EOF before expected region end");
			}
			o.write(buf,0,amtRead);
			left -= amtRead;
		}
		raf.close();
	}
}
