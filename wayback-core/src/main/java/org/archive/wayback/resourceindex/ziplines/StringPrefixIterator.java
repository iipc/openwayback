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

import java.io.IOException;
import java.util.Iterator;

import org.archive.util.iterator.CloseableIterator;



/**
 * Iterator<String> decorator, which assumes the decorated is in SORTED order.
 * This iterator will discard all elements in the iterator LESS than prefix 
 * constructor argument, return all elements STARTING with prefix, and stop
 * iterating when a record is GREATER than prefix.
 * 
 * @author brad
 *
 */
public class StringPrefixIterator implements CloseableIterator<String> {
	protected String prefix = null;
	protected Iterator<String> inner = null;
	protected String cachedNext = null;
	protected boolean done = false;
	protected boolean truncated = false;
	
	public StringPrefixIterator(Iterator<String> inner, String prefix) {
		this.prefix = prefix;
		this.inner = inner;
		if(inner instanceof ZiplinesChunkIterator) {
			truncated = ((ZiplinesChunkIterator)inner).isTruncated();
		}
	}
	public long getTotalMatches() {
		return 0 ;
	}
	public boolean isTruncated() {
		return truncated;
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
