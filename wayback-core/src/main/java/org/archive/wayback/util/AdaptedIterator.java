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
import java.util.NoSuchElementException;

import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.CloseableIteratorUtil;

/**
 * Iterator that transforms objects of one type to another.
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <S> 
 * @param <T> 
 */
public class AdaptedIterator<S,T> implements CloseableIterator<T> {
	protected Iterator<S> itr;
	protected Adapter<S,T> adapter;
	private T cachedNext = null;
	/**
	 * @param itr
	 * @param adapter
	 */
	public AdaptedIterator(Iterator<S> itr, Adapter<S,T> adapter) {
		this.itr = itr;
		this.adapter = adapter;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(cachedNext != null) return true;
		while(itr.hasNext()) {
			S o = itr.next();
			T adapted = adapter.adapt(o);
			if(adapted != null) {
				cachedNext = adapted;
				return true;
			}
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public T next() {
		if(cachedNext == null) {
			throw new NoSuchElementException("call hasNext first!");
		}
		T o = cachedNext;
		cachedNext = null;
		return o;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		itr.remove();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Cleanable#clean()
	 */
	public void close() throws IOException {
		CloseableIteratorUtil.attemptClose(itr);
	}
}
