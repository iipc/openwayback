/* ReverseBufferedReader
 *
 * $Id$
 *
 * Created on 2:10:35 PM Aug 17, 2006.
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
