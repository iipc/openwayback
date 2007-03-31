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


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Composite of multiple Iterators that returns the next from a series of
 * all component Iterators based on Comparator constructor argument.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeSortedIterator implements CloseableIterator {

	private ArrayList<PeekableIterator> components;
	private Object next;
	private Comparator<Object> comparator;
	
	/**
	 * @param comparator Comparator to use for sorting order
	 */
	public CompositeSortedIterator(Comparator<Object> comparator) {
		this.comparator = comparator;
		components = new ArrayList<PeekableIterator>();
		next = null;
	}
	/**
	 * @param itr Iterator which is a component of this composite
	 */
	public void addComponent(Iterator itr) {
		components.add(new PeekableIterator(itr));	
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(next != null) {
			return true;
		}
		// find lowest next:
		PeekableIterator nextSource = null;
		for(int i = 0; i < components.size(); i++) {
			PeekableIterator pi = components.get(i);
			if(pi.hasNext()) {
				Object piNext = pi.peekNext();
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
	public Object next() {
		if(!hasNext()) {
			throw new NoSuchElementException();
		}
		Object retObject = next;
		next = null;
		return retObject;
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new NotImplementedException();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Cleanable#clean()
	 */
	public void close() throws IOException {
		for(int i = 0; i < components.size(); i++) {
			PeekableIterator pi = (PeekableIterator) components.get(i);
			pi.close();
		}
	}

	private class PeekableIterator implements CloseableIterator {
		private Object cachedNext;
		private Iterator itr;
		/**
		 * @param itr
		 */
		public PeekableIterator(Iterator itr) {
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
		public Object peekNext() {
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
		public Object next() {
			if(cachedNext != null) {
				Object retObject = cachedNext;
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
				CloseableIterator toBeClosed = (CloseableIterator) itr;
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
}
