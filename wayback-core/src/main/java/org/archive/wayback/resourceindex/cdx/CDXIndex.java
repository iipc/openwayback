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
package org.archive.wayback.resourceindex.cdx;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.SortedCompositeIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.resourceindex.SearchResultSource;
import org.archive.wayback.util.AdaptedIterator;
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

	protected CloseableIterator<CaptureSearchResult> adaptIterator(Iterator<String> itr)
		throws IOException {
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
		SortedCompositeIterator<CaptureSearchResult> itr = 
			new SortedCompositeIterator<CaptureSearchResult>(comparator);
		itr.addIterator(forwardItr);
		itr.addIterator(reverseItr);
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
