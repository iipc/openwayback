/* LocalCDXResourceIndex
 *
 * $Id$
 *
 * Created on 2:15:12 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex.cdx;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.SearchResultSource;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.CompositeSortedIterator;
import org.archive.wayback.util.flatfile.FlatFile;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class CDXIndex extends FlatFile implements SearchResultSource {

	/**
	 * @param parent
	 * @param child
	 */
	public CDXIndex(File parent, String child) {
		super(parent, child);
	}
	/**
	 * @param path
	 */
	public CDXIndex(String path) {
		super(path);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CloseableIterator adaptIterator(Iterator itr) {
		return new AdaptedIterator(itr,new CDXLineToSearchResultAdapter());
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixIterator(String prefix)
		throws ResourceIndexNotAvailableException {
		try {
			return adaptIterator(getRecordIterator(prefix));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixReverseIterator(java.lang.String)
	 */
	public CloseableIterator getPrefixReverseIterator(String prefix)
		throws ResourceIndexNotAvailableException {
		try {
			return adaptIterator(getReverseRecordIterator(prefix));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceIndexNotAvailableException(e.getMessage()); 
		}
	}

	/**
	 * @param prefix
	 * @return Iterator of SearchResults of records starting with prefix
	 * @throws IOException
	 */
	public Iterator getUrlIterator(final String prefix) throws IOException {
		return adaptIterator(getRecordIterator(prefix));
	}
	
	/**
	 * @param prefix
	 * @param wantTS
	 * @return Iterator of results in closest order to wantTS
	 * @throws IOException
	 */
	public Iterator getClosestIterator(final String prefix, 
			final Timestamp wantTS) throws IOException {
		
		Iterator forwardItr = adaptIterator(getRecordIterator(prefix));
		Iterator reverseItr = adaptIterator(getReverseRecordIterator(prefix));
		TimestampComparator comparator = new TimestampComparator(wantTS);
		CompositeSortedIterator itr = new CompositeSortedIterator(comparator);
		itr.addComponent(forwardItr);
		itr.addComponent(reverseItr);
		return itr;
	}

	private class TimestampComparator implements Comparator<Object> {
		private int wantedSSE;
		/**
		 * @param wanted
		 */
		public TimestampComparator(Timestamp wanted) {
			wantedSSE = wanted.sse();
		}
		private int searchResultToDistance(Object o) {
			if(!(o instanceof SearchResult)) {
				throw new IllegalArgumentException("Need SearchResult objects");
			}
			SearchResult sr = (SearchResult) o;
			String dateStr = sr.get(WaybackConstants.RESULT_CAPTURE_DATE);
			Timestamp ts = new Timestamp(dateStr);
			return Math.abs(wantedSSE - ts.sse());
		}
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			int d1 = searchResultToDistance(o1);
			int d2 = searchResultToDistance(o2);
			if(d1 < d2) {
				return -1;
			} else if(d1 > d2) {
				return 1;
			}
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#cleanup(org.archive.wayback.util.CleanableIterator)
	 */
	public void cleanup(CloseableIterator c) throws IOException {
		c.close();
	}
}
