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
package org.archive.wayback.util;

import java.io.IOException;
import java.util.Iterator;

import org.archive.util.iterator.CloseableIterator;

/**
 * Simple wrapper around a normal Iterator which allows use of the close().
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WrappedCloseableIterator<E> implements CloseableIterator<E> {

	private Iterator<E> inner = null;
	
	public WrappedCloseableIterator(Iterator<E> inner) {
		this.inner = inner;
	}

	public boolean hasNext() {
		return inner.hasNext();
	}

	public E next() {
		return inner.next();
	}

	public void remove() {
		inner.remove();
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		// NO-OP
	}
	
}
