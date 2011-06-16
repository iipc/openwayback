package org.archive.wayback.util.iterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;


public abstract class AbstractPeekableIterator<E> implements IPeekableIterator<E> {
	private E cachedNext = null; 
	private boolean done = false;

	// returns next E, or null if hasNext() would return false;
	public abstract E getNextInner();
	
	public boolean hasNext() {
		if(cachedNext != null) {
			return true;
		}
		if(done) {
			return false;
		}
		cachedNext = getNextInner();
		return (cachedNext != null);
	}

	public E next() {
		if(cachedNext == null) {
			if(!hasNext()) {
				throw new NoSuchElementException("Call hasNext!");
			}
		}
		E tmp = cachedNext;
		cachedNext = null;
		return tmp;
	}

	public void remove() {
		throw new UnsupportedOperationException("No remove");
	}

	public E peek() {
		if(cachedNext == null) {
			if(!hasNext()) {
				throw new NoSuchElementException("Call hasNext!");
			}
		}
		return cachedNext;
	}
	public static <T> IPeekableIterator<T> wrap(Iterator<T> itr) {
		return new IteratorWrappedPeekableIterator<T>(itr);
	}
	public static IPeekableIterator<String> wrapReader(BufferedReader reader) {
		return new BufferedReaderPeekableIterator(reader);
	}
	
	private static class IteratorWrappedPeekableIterator<C> extends AbstractPeekableIterator<C> {
		private Iterator<C> wrapped = null;
		public IteratorWrappedPeekableIterator(Iterator<C> wrapped) {
			this.wrapped = wrapped;
		}
		@Override
		public C getNextInner() {
			C next = null;
			if(wrapped != null) {
				if(wrapped.hasNext()) {
					next = wrapped.next();
				}
			}
			return next;
		}
	}
	private static class BufferedReaderPeekableIterator extends AbstractPeekableIterator<String> {
		private BufferedReader reader = null;
		public BufferedReaderPeekableIterator(BufferedReader reader) {
			this.reader = reader;
		}
		@Override
		public String getNextInner() {
			String next = null;
			if(reader != null) {
				try {
					next = reader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return next;
		}
	}

}
