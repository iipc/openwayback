/* AdaptedIterator
 *
 * $Id$
 *
 * Created on 2:39:07 PM Aug 17, 2006.
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
package org.archive.wayback.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

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
	@SuppressWarnings("unchecked")
	public void close() throws IOException {
		if(itr instanceof CloseableIterator) {
			CloseableIterator<S> toBeClosed = (CloseableIterator<S>) itr;
			toBeClosed.close();
		}
	}
}
