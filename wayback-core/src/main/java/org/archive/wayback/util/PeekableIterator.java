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
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public class PeekableIterator<E> implements CloseableIterator<E> {
	private E cachedNext;
	private Iterator<E> itr;
	/**
	 * @param itr
	 */
	public PeekableIterator(Iterator<E> itr) {
		this.itr = itr;
		this.cachedNext = null;
	}
	/**
	 * @return true if this Iterator has another element.
	 */
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		return itr.hasNext();
	}
	/**
	 * @return Object that will be returned from next(), or null
	 */
	public E peekNext() {
		if(cachedNext == null) {
			if(itr.hasNext()) {
				cachedNext = itr.next();
			}
		}
		return cachedNext;
	}
	/**
	 * @return next Object 
	 */
	public E next() {
		if(cachedNext != null) {
			E retObject = cachedNext;
			cachedNext = null;
			return retObject;
		}
		return itr.next();
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Cleanable#clean()
	 */
	@SuppressWarnings("unchecked")
	public void close() throws IOException {
		if(itr instanceof CloseableIterator) {
			CloseableIterator<E> toBeClosed = (CloseableIterator<E>) itr;
			toBeClosed.close();
		}
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
