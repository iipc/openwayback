/* Resource
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.ChunkedInputStream;

/**
 * Abstraction on top of a document stored in a WaybackCollection. Currently
 * implemented subclasses include ArcResource and WarcResource.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public abstract class Resource extends InputStream {
	
	private InputStream is;

    public abstract void close() throws IOException;
	public abstract int getStatusCode();
	public abstract long getRecordLength();
	public abstract Map<String,String> getHttpHeaders();

	private void validate() throws IOException {
		if(is == null) {
			throw new IOException("No InputStream");
		}
	}

	protected void setInputStream(InputStream is) {
		if(is.markSupported()) {
			this.is = is;
		} else {
			this.is = new BufferedInputStream(is);
		}
	}
	/**
	 * indicate that there is a Transfer-Encoding: chunked header, so the input
	 *   data should be dechunked as it is read.
	 * @throws IOException
	 */
	public void setChunkedEncoding() throws IOException {
		validate();
		// peek ahead and make sure we have a line with hex numbers:
		int max = 50;
		is.mark(max+2);
		int cur = 0;
		boolean isChunked = false;
		while(cur < max) {
			int nextC = is.read();
			if(nextC == 10) {
				// must have read at least 1 hex char:
				if(cur > 0) {
					nextC = is.read();
					if(nextC == 13) {
						isChunked = true;
						break;
					}
				}
			} else {
				// better be a hex character:
				if(!isHex(nextC)) {
					break;
				}
			}
			cur++;
		}
		is.reset();
		if(isChunked) {
			is = new ChunkedInputStream(is);
		}
	}
	
	private boolean isHex(int c) {
		if((c >= '0') && (c <= '9')) {
			return true;
		}
		if((c >= 'a') && (c <= 'f')) {
			return true;
		}
		if((c >= 'A') && (c <= 'F')) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#available()
	 */
	public int available() throws IOException {
		validate();
		return is.available();
	}
	/**
	 * @param readlimit
	 * @see java.io.BufferedInputStream#mark(int)
	 */
	public void mark(int readlimit) {
		if(is != null) {
			is.mark(readlimit);
		}
	}
	/**
	 * @return
	 * @see java.io.BufferedInputStream#markSupported()
	 */
	public boolean markSupported() {
		if(is == null) {
			return false;
		}
		return is.markSupported();
	}
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#read()
	 */
	public int read() throws IOException {
		validate();
		return is.read();
	}
	/**
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		validate();
		return is.read(b, off, len);
	}
	/**
	 * @param b
	 * @return
	 * @throws IOException
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		validate();
		return is.read(b);
	}
	/**
	 * @throws IOException
	 * @see java.io.BufferedInputStream#reset()
	 */
	public void reset() throws IOException {
		validate();
		is.reset();
	}
	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		validate();
		return is.skip(n);
	}
}
