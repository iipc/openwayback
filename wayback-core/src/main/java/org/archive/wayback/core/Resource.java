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
 * This implementation needs some pretty drastic refactoring.. May have to wait
 * for 2.0. This should be a byte-oriented record, and allow wrapping the 
 * interior byte-stream in on the more full featured HTTP libraries 
 * (jetty/apache-http-client/w3c-http-reference).
 * 
 * For now, it is a system-wide assumption that all resources are HTTP based.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public abstract class Resource extends InputStream {
	
	private InputStream is;

    public abstract void close() throws IOException;
	/**
	 * Assumes an HTTP resource - return the HTTP response code
	 * @return the HTTP response code from the HTTP message
	 */
	public abstract int getStatusCode();
	/**
	 * @return the size in bytes of the record payload, including HTTP header
	 */
	public abstract long getRecordLength();
	/**
	 * Assumes an HTTP response - return the HTTP headers, not including the
	 * HTTP Message header
	 * @return key-value Map of HTTP headers 
	 */
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
	 *   data should be dechunked as it is read. This method actually peeks
	 *   ahead to verify that there is a hex-encoded chunk length before
	 *   assuming the data is chunked.
	 * @throws IOException for usual reasons
	 */
	public void setChunkedEncoding() throws IOException {
		validate();
		// peek ahead and make sure we have a line with hex numbers:
		int max = 50;
		is.mark(max+2);
		int cur = 0;
		int hexFound = 0;
		boolean isChunked = false;
		while(cur < max) {
			int nextC = is.read();
			// allow CRLF and plain ole LF:
			if((nextC == 13) || (nextC == 10)) {
				// must have read at least 1 hex char:
				if(hexFound > 0) {
					if(nextC == 10) {
						isChunked = true;
						break;
					}
					nextC = is.read();
					if(nextC == 10) {
						isChunked = true;
						break;
					}
				}
				// keep looking to allow some blank lines. 
			} else {
				// better be a hex character:
				if(isHex(nextC)) {
					hexFound++;
				} else if(nextC != ' ') {
					// allow whitespace before or after chunk...
					// not a hex digit: not a chunked stream.
					break;
				}
			}
			cur++;
		}
		is.reset();
		if(isChunked) {
			setInputStream(new ChunkedInputStream(is));
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

	public int available() throws IOException {
		validate();
		return is.available();
	}

	public void mark(int readlimit) {
		if(is != null) {
			is.mark(readlimit);
		}
	}

	public boolean markSupported() {
		if(is == null) {
			return false;
		}
		return is.markSupported();
	}

	public int read() throws IOException {
		validate();
		return is.read();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		validate();
		return is.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		validate();
		return is.read(b);
	}

	public void reset() throws IOException {
		validate();
		is.reset();
	}

	public long skip(long n) throws IOException {
		validate();
		return is.skip(n);
	}
}
