package org.archive.wayback.util.iterator;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;


public class SortedCompositeIterator<E> implements Iterator<E> {
	private static final int DEFAULT_CAPACITY = 10;
	PriorityQueue<IPeekableIterator<E>> q = null;

	public SortedCompositeIterator(Comparator<E> comparator) {
		this(DEFAULT_CAPACITY,comparator);
	}
	public SortedCompositeIterator(int capacity, Comparator<E> comparator) {
		q = new PriorityQueue<IPeekableIterator<E>>(capacity, 
				new PeekableIteratorComparator<E>(comparator));
	}
	public void addAll(Collection<Iterator<E>> toAdd) {
		for(Iterator<E> e : toAdd) {
			addIterator(e);
		}
	}
	public void addIterator(Iterator<E> itr) {
		IPeekableIterator<E> i = null;
		if(itr instanceof IPeekableIterator) {
			i = (IPeekableIterator<E>) itr;
		} else {
			i = AbstractPeekableIterator.wrap(itr);
		}
		if(i.hasNext()) {
			q.add(i);
		}
	}

	public boolean hasNext() {
		return (q.peek() != null);
	}

	public E next() {
		IPeekableIterator<E> i = q.poll();
		if(i == null) {
			throw new NoSuchElementException("Call hasNext!");
		}
		E tmp = i.next();
		if(i.hasNext()) {
			q.add(i);
		}
		return tmp;
	}
	public void remove() {
		throw new UnsupportedOperationException("No remove");
	}
}
