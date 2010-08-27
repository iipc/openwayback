/* ZiplinesChunkIterator
 *
 * $Id$:
 *
 * Created on Nov 23, 2009.
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
import java.util.zip.GZIPInputStream;

import org.archive.wayback.exception.RuntimeIOException;
import org.archive.wayback.util.CloseableIterator;

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
				BufferedReader br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(raf.getFD()))));
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
