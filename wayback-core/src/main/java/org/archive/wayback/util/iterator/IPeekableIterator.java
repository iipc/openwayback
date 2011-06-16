package org.archive.wayback.util.iterator;

import java.util.Iterator;

public interface IPeekableIterator<E> extends Iterator<E> {
	public E peek();
}
