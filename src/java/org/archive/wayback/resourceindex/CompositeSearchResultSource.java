/* CompositeSearchResultSource
 *
 * $Id$
 *
 * Created on 4:34:49 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.CompositeSortedIterator;

/**
 * SearchResultSource that aggregates results from multiple SearchResultSources.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeSearchResultSource implements SearchResultSource {

	protected ArrayList<SearchResultSource> sources;

	/**
	 * Constructor
	 */
	public CompositeSearchResultSource() {
		sources = new ArrayList<SearchResultSource>();
	}

	/**
	 * add a SearchResultSource to this composite
	 * 
	 * @param source
	 *            to be added
	 */
	public void addSource(SearchResultSource source) {
		sources.add(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixIterator(String prefix)
			throws ResourceIndexNotAvailableException {

		Comparator<Object> comparator = new SearchResultComparator();
		CompositeSortedIterator itr = new CompositeSortedIterator(comparator);
		for (int i = 0; i < sources.size(); i++) {
			itr.addComponent(sources.get(i).getPrefixIterator(prefix));
		}
		return itr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixReverseIterator(String prefix)
			throws ResourceIndexNotAvailableException {

		Comparator<Object> comparator = new SearchResultComparator(true);
		CompositeSortedIterator itr = new CompositeSortedIterator(comparator);
		for (int i = 0; i < sources.size(); i++) {
			itr.addComponent(sources.get(i).getPrefixReverseIterator(prefix));
		}
		return itr;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CleanableIterator)
	 */
	public void cleanup(CloseableIterator c) throws IOException{
		c.close();
	}
}
