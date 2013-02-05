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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;
import org.archive.util.zip.OpenJDK7GZIPInputStream;
import org.archive.wayback.exception.RuntimeIOException;
import org.archive.wayback.util.ByteOp;

/**
 * @author brad
 *
 */
public class ZiplinesChunkIterator implements CloseableIterator<String> {
	private static final Logger LOGGER = Logger.getLogger(
			ZiplinesChunkIterator.class.getName());

	private BufferedReader br = null;
	private Iterator<ZiplinedBlock> blockItr = null;
	private String cachedNext = null;
	private boolean truncated = false;
	/**
	 * @param blocks which should be fetched and unzipped, one after another
	 */
	public ZiplinesChunkIterator(List<ZiplinedBlock> blocks) {
		LOGGER.info("initialized with " + blocks.size() + " blocks");
		blockItr = blocks.iterator();
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		while(cachedNext == null) {
			if(br != null) {
				// attempt to read the next line from this:
				try {
					cachedNext = br.readLine();
					if(cachedNext == null) {
						br = null;
						// next loop:
					} else {
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
					br = null;
				}
			} else {
				// do we have more blocks to use?
				if(blockItr.hasNext()) {
					try {
						br = blockItr.next().readBlock();
					} catch (IOException e) {
						throw new RuntimeIOException();
					}
				} else {
					return false;
				}
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public String next() {
		String tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if(br != null) {
			br.close();
		}
	}
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: ZIPLINES_PATH");
			System.exit(1);
		}
		File f = new File(args[0]);
		long size = f.length();
		long numBlocks = (long) (size / ZiplinedBlock.BLOCK_SIZE);
		long size2 = numBlocks * ZiplinedBlock.BLOCK_SIZE;
		if(size != size2) {
			System.err.println("File size of " + args[0] + " is not a mulitple"
					+ " of " + ZiplinedBlock.BLOCK_SIZE);
		}
		try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			for(int i = 0; i < numBlocks; i++) {
				long offset = i * ZiplinedBlock.BLOCK_SIZE;
				raf.seek(offset);
//				BufferedReader br = new BufferedReader(new InputStreamReader(
//						new GZIPInputStream(new FileInputStream(raf.getFD())),ByteOp.UTF8));
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new OpenJDK7GZIPInputStream(new FileInputStream(raf.getFD())),ByteOp.UTF8));
				String line = br.readLine();
				if(line == null) {
					System.err.println("Bad block at " + offset + " in " + args[0]);
					System.exit(1);
				}
				System.out.println(args[0] + " " + offset + " " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * @return the truncated
	 */
	public boolean isTruncated() {
		return truncated;
	}
	/**
	 * @param truncated the truncated to set
	 */
	public void setTruncated(boolean truncated) {
		this.truncated = truncated;
	}
}
