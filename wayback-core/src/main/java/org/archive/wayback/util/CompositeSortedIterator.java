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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.archive.util.iterator.CloseableIterator;


/**
 * Composite of multiple Iterators that returns the next from a series of
 * all component Iterators based on Comparator constructor argument.
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public class CompositeSortedIterator<E> implements CloseableIterator<E> {

	private final static Logger LOGGER = Logger.getLogger(CompositeSortedIterator.class.getName());
	
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
			PeekableIterator<E> pi = components.get(i);
			
			// Catch exception so that we can still close others
			try {
				pi.close();
			} catch (IOException io) {
				LOGGER.warning(io.toString());
			}
		}
	}
}
