/* FileLocationDBLog
 *
 * $Id$
 *
 * Created on 2:38:18 PM Aug 18, 2006.
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
package org.archive.wayback.resourcestore.locationdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.archive.wayback.util.CloseableIterator;
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
		BufferedReader is = new BufferedReader(new FileReader(raf.getFD()));
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
