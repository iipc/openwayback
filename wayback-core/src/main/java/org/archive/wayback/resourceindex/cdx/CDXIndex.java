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

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import org.archive.wayback.core.CaptureSearchResult;
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
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private CloseableIterator<CaptureSearchResult> adaptIterator(Iterator<String> itr) {
		return new AdaptedIterator<String,CaptureSearchResult>(itr,
				new CDXLineToSearchResultAdapter());
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultSource#getPrefixIterator(java.lang.String)
	 */
	public CloseableIterator<CaptureSearchResult> getPrefixIterator(String prefix)
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
	public CloseableIterator<CaptureSearchResult> getPrefixReverseIterator(String prefix)
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
	 * @return Iterator of CaptureSearchResult of records starting with prefix
	 * @throws IOException
	 */
	public Iterator<CaptureSearchResult> getUrlIterator(final String prefix) throws IOException {
		return adaptIterator(getRecordIterator(prefix));
	}
	
	/**
	 * @param prefix
	 * @param wantTS
	 * @return Iterator of results in closest order to wantTS
	 * @throws IOException
	 */
	public Iterator<CaptureSearchResult> getClosestIterator(final String prefix, 
			final Date wantDate) throws IOException {
		
		Iterator<CaptureSearchResult> forwardItr = adaptIterator(getRecordIterator(prefix));
		Iterator<CaptureSearchResult> reverseItr = adaptIterator(getReverseRecordIterator(prefix));
		Comparator<CaptureSearchResult> comparator = new CaptureSRComparator(wantDate);
		CompositeSortedIterator<CaptureSearchResult> itr = 
			new CompositeSortedIterator<CaptureSearchResult>(comparator);
		itr.addComponent(forwardItr);
		itr.addComponent(reverseItr);
		return itr;
	}

	private class CaptureSRComparator implements Comparator<CaptureSearchResult> {
		private long wantTime;
		/**
		 * @param wanted
		 */
		public CaptureSRComparator(Date wanted) {
			wantTime = wanted.getTime();
		}
		private long searchResultToDistance(CaptureSearchResult sr) {
			return Math.abs(wantTime - sr.getCaptureDate().getTime());
		}
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(CaptureSearchResult o1, CaptureSearchResult o2) {
			long d1 = searchResultToDistance(o1);
			long d2 = searchResultToDistance(o2);
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
	public void cleanup(CloseableIterator<CaptureSearchResult> c) throws IOException {
		c.close();
	}

	public void shutdown() throws IOException {
		// no-op
	}
}
