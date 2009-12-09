/* StringPrefixIterator
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

import java.io.IOException;
import java.util.Iterator;

import org.archive.wayback.util.CloseableIterator;

/**
 * @author brad
 *
 */
public class StringPrefixIterator implements CloseableIterator<String> {
	private String prefix = null;
	Iterator<String> inner = null;
	private String cachedNext = null;
	private boolean done = false;
	public StringPrefixIterator(Iterator<String> inner, String prefix) {
		this.prefix = prefix;
		this.inner = inner;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(done) return false;
		if(cachedNext != null) {
			return true;
		}
		while(inner.hasNext()) {
			String tmp = inner.next();
			if(tmp.startsWith(prefix)) {
				cachedNext = tmp;
				return true;
			} else if(tmp.compareTo(prefix) > 0) {
				done = true;
				return false;
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
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		if(inner instanceof CloseableIterator) {
			CloseableIterator<String> toBeClosed = (CloseableIterator<String>) inner;
			toBeClosed.close();
		}
	}
}
