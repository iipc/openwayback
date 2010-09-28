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
package org.archive.wayback.util.flatfile;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Just like a BufferedReader, except the buffer scrolls backvards, allowing
 * this one to support 'readPrevLine()' instead of readLine(). 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ReverseBufferedReader {
	private static int DEFAULT_BUFFER_SIZE = 4096;
	
	// if the StringBuilder 'buffer' ever gets larger than 
	// MAX_BUFFER_MULTIPLE * bufferSize, fail: it means there is a line that's
	// just too big to handle.
	private static int MAX_BUFFER_MULTIPLE = 10;

	private static char NEWLINE = '\n';
	//private static char LINEFEED = '\r';
	private StringBuilder buffer;
	private RandomAccessFile raf;
	private int bufferSize = DEFAULT_BUFFER_SIZE;
	private long lastOffset;
	private boolean hitBOF;
	private String lineDelimiter;
	/**
	 * @param raf
	 * @throws IOException
	 */
	public ReverseBufferedReader(RandomAccessFile raf) throws IOException {
		this.raf = raf;
		lastOffset = raf.getFilePointer();
		hitBOF = lastOffset == 0;
		lineDelimiter = String.valueOf(NEWLINE);
		buffer = new StringBuilder(bufferSize);
	}
	/**
	 * @param bufferSize The bufferSize to set.
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}
	private boolean getMoreData() throws IOException {
		byte[] buf = new byte[bufferSize];
		if(lastOffset == 0) {
			hitBOF = true;
			return false;
		}
		// make sure we don't run out of RAM from a seriously big line:
		if(buffer.length() > (bufferSize * MAX_BUFFER_MULTIPLE)) {
			hitBOF = true;
			return false;
		}
		long nextOffset = lastOffset - bufferSize;
		if(nextOffset < 0) nextOffset = 0;
		// TODO: is (int) conversion safe here?
		int amountToRead = (int) (lastOffset - nextOffset);
		raf.seek(nextOffset);
		raf.read(buf,0,amountToRead);
		lastOffset = nextOffset;
		buffer.insert(0,new String(buf,0,amountToRead,"UTF-8"));
		return true;
	}
	/**
	 * @return String previous line read from the file
	 * @throws IOException
	 */
	public String readPrevLine() throws IOException {
		if(hitBOF) return null;
		int lastFound;
		while(true) {
			lastFound = buffer.lastIndexOf(lineDelimiter);
			if(lastFound == -1) {
				if(!getMoreData()) return null;
				// no more data!
			} else {
				// still in business with current buffer...
				String ret = buffer.substring(lastFound+1);
				buffer.setLength(lastFound);
				return ret;
			}
		}
	}
	/**
	 * @throws IOException
	 */
	public void close() throws IOException {
		raf.close();
	}
}
