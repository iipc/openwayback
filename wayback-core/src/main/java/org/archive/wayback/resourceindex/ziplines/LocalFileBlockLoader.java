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
package org.archive.wayback.resourceindex.ziplines;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Simple block loader which uses RandomAccessFiles to grab ranges of local 
 * files.
 * @author brad
 *
 */
public class LocalFileBlockLoader implements BlockLoader {

	public byte[] getBlock(String url, long offset, int length)
			throws IOException {
		File file = new File(url);
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(offset);
		if(raf.getFilePointer() != offset) {
			throw new IOException("Failed seek("+offset+") in ("+url+")");
		}
		byte b[] = new byte[length];
		raf.readFully(b);
		raf.close();
		return b;
	}

}
