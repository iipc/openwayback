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

	protected void setInputStream(InputStream is) {
		if(is.markSupported()) {
			this.is = is;
		} else {
			this.is = new BufferedInputStream(is);
		}
	}
	/**
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#available()
	 */
	public int available() throws IOException {
		if(is == null) {
			throw new IOException("No InputStream");
		}
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
		if(is == null) {
			throw new IOException("No InputStream");
		}
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
		if(is == null) {
			throw new IOException("No InputStream");
		}
		return is.read(b, off, len);
	}
	/**
	 * @param b
	 * @return
	 * @throws IOException
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		if(is == null) {
			throw new IOException("No InputStream");
		}
		return is.read(b);
	}
	/**
	 * @throws IOException
	 * @see java.io.BufferedInputStream#reset()
	 */
	public void reset() throws IOException {
		if(is == null) {
			throw new IOException("No InputStream");
		}
		is.reset();
	}
	/**
	 * @param n
	 * @return
	 * @throws IOException
	 * @see java.io.BufferedInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		if(is == null) {
			throw new IOException("No InputStream");
		}
		return is.skip(n);
	}
}
