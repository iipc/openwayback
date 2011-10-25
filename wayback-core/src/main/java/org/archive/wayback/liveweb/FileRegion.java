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
		try {
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
		} finally {
			raf.close();
		}
	}
}
