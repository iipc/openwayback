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

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ObjectFilterIterator<T> implements CloseableIterator<T> {

	ObjectFilter<T> filter = null;
	Iterator<T> itr = null;
	boolean aborted = false;
	T cachedNext = null;
	public ObjectFilterIterator(Iterator<T> itr, 
			ObjectFilter<T> filter) {
		this.itr = itr;
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		if(aborted) {
			return false;
		}
		while(cachedNext == null) {
			if(!itr.hasNext()) {
				aborted = true;
				close();
				return false;
			}
			T maybeNext = itr.next();
			int ruling = filter.filterObject(maybeNext);
			if(ruling == ObjectFilter.FILTER_ABORT) {
				aborted = true;
				close();
				return false;
			} else if(ruling == ObjectFilter.FILTER_INCLUDE) {
				cachedNext = maybeNext;
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public void close() {
		if(itr instanceof CloseableIterator) {
			CloseableIterator<T> citr =
				(CloseableIterator<T>) itr;
			try {
				citr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		// TODO Auto-generated method stub
		
	}
}
