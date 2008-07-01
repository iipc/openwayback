/* ObjectFilterIterator
 *
 * $Id$
 *
 * Created on 2:55:48 PM Jun 28, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
	public ObjectFilterIterator(CloseableIterator<T> itr, 
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
