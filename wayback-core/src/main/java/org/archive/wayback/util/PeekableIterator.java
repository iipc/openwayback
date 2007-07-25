/* PeekableIterator
 *
 * $Id$
 *
 * Created on 4:37:15 PM Jul 24, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util;

import java.io.IOException;
import java.util.Iterator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
		throw new NotImplementedException();
	}
}
