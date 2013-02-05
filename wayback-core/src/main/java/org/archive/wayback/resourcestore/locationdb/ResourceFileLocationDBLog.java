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
package org.archive.wayback.resourcestore.locationdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.flatfile.RecordIterator;

/**
 * Simple log file tracking new names being added to a ResourceFileLocationDB.
 * 
 * Also supports returning an iterator of Strings to a byte range of the log, to
 * simplify tracking deltas to the DB.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileLocationDBLog extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9128222006544481378L;

	/**
	 * @param pathname
	 * @throws IOException
	 */
	public ResourceFileLocationDBLog(String pathname) throws IOException {
		super(pathname);
		if (!isFile()) {
			if (exists()) {
				throw new IOException("path(" + pathname
						+ ") exists but is not a file!");
			}
			try {
				if (!createNewFile()) {
					throw new IOException(
							"Unable to create empty file " + pathname);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new IOException("Unable to create empty file "
						+ pathname);
			}
		}
	}

	/**
	 * @return long value indicating the current end position of the log
	 */
	public long getCurrentMark() {
		return length();
	}

	/**
	 * @param start
	 * @param end
	 * @return CleanableIterator that returns all names between start and end
	 * @throws IOException
	 */
	public CloseableIterator<String> getNamesBetweenMarks(long start, long end)
			throws IOException {

		RandomAccessFile raf = new RandomAccessFile(this, "r");
		raf.seek(start);
		FileInputStream fis = new FileInputStream(raf.getFD());
		InputStreamReader isr = new InputStreamReader(fis,ByteOp.UTF8);
		BufferedReader is = new BufferedReader(isr);
		return new BufferedRangeIterator(new RecordIterator(is),end - start);
	}

	/**
	 * @param name
	 * @throws IOException
	 */
	public synchronized void addName(String name) throws IOException {
		FileWriter writer = new FileWriter(this, true);
		writer.write(name + "\n");
		writer.flush();
		writer.close();
	}

	private class BufferedRangeIterator implements CloseableIterator<String> {
		private RecordIterator itr;
		private long bytesToSend;
		private long bytesSent;
		private String next;
		private boolean done;
		/**
		 * @param itr
		 * @param bytesToSend
		 */
		public BufferedRangeIterator(RecordIterator itr, long bytesToSend) {
			this.itr = itr;
			this.bytesToSend = bytesToSend;
			bytesSent = 0;
			next = null;
			done = false;
		}
		/* (non-Javadoc)
		 * @see org.archive.wayback.util.CleanableIterator#clean()
		 */
		public void close() throws IOException {
			if(done == false) {
				itr.close();
				done = true;
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			if(done) return false;
			if(next != null) return true;
			if((bytesSent >= bytesToSend) || !itr.hasNext()) {
				try {
					close();
				} catch (IOException e) {
					// TODO This is lame. What is the right way?
					throw new RuntimeException(e);
				}
				return false;
			}
			next = (String) itr.next();
			return true;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public String next() {
			String returnString = next;
			next = null;
			bytesSent += returnString.length() + 1; // TODO: not X-platform!
			return returnString;
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
