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
package org.archive.wayback.util.iterator;

import java.util.Comparator;

/**
 * @author brad
 *
 * @param <J> type found in Iterators
 */
public class PeekableIteratorComparator<J> implements Comparator<IPeekableIterator<J>> {
	private Comparator<J> comparator = null;
	/**
	 * @param comparator to compare the iterators
	 */
	public PeekableIteratorComparator(Comparator<J> comparator) {
		this.comparator = comparator;
	}

	public int compare(IPeekableIterator<J> o1, IPeekableIterator<J> o2) {
		return comparator.compare(o1.peek(), o2.peek());
	}
	public static <K> Comparator<IPeekableIterator<K>> getComparator(Comparator<K> comparator) {
		return new PeekableIteratorComparator<K>(comparator);
	}
}
