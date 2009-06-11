/* CompositeSortedIterator
 *
 * $Id$
 *
 * Created on 2:50:01 PM Aug 17, 2006.
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Composite of multiple Iterators that returns the next from a series of
 * all component Iterators based on Comparator constructor argument.
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public class CompositeSortedIterator<E> implements CloseableIterator<E> {

	private ArrayList<PeekableIterator<E>> components;
	private E next;
	private Comparator<E> comparator;
	
	/**
	 * @param comparator Comparator to use for sorting order
	 */
	public CompositeSortedIterator(Comparator<E> comparator) {
		this.comparator = comparator;
		components = new ArrayList<PeekableIterator<E>>();
		next = null;
	}
	/**
	 * @param itr Iterator which is a component of this composite
	 */
	public void addComponent(Iterator<E> itr) {
		components.add(new PeekableIterator<E>(itr));	
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(next != null) {
			return true;
		}
		// find lowest next:
		PeekableIterator<E> nextSource = null;
		for(int i = 0; i < components.size(); i++) {
			PeekableIterator<E> pi = components.get(i);
			if(pi.hasNext()) {
				E piNext = pi.peekNext();
				if((next == null) || (comparator.compare(next,piNext) > 0)) {
					nextSource = pi;
					next = piNext;
				}
			}
		}
		if(nextSource != null) {
			nextSource.next();
		}
		return next != null;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public E next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		E retObject = next;
		next = null;
		return retObject;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Cleanable#clean()
	 */
	public void close() throws IOException {
		for(int i = 0; i < components.size(); i++) {
			PeekableIterator<E> pi = (PeekableIterator<E>) components.get(i);
			pi.close();
		}
	}
}
