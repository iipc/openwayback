/* ArcIndexer
 *
 * $Id$
 *
 * Created on 2:33:29 PM Oct 11, 2006.
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
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;

import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 * Transforms an ARC file into Iterator<CaptureSearchResult>.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcIndexer {

	private UrlCanonicalizer canonicalizer = null;
	
	public ArcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(File arc)
	throws IOException {
		return iterator(ARCReaderFactory.get(arc));
	}

	/**
	 * @param pathOrUrl
	 * @return Iterator of SearchResults for input pathOrUrl
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(String pathOrUrl)
	throws IOException {
		File f = new File(pathOrUrl);
		if(f.isFile()) {
			return iterator(ARCReaderFactory.get(f));
		} else {
			return iterator(ARCReaderFactory.get(pathOrUrl));
		}
	}
	
	/**
	 * @param arcReader
	 * @return Iterator of SearchResults for input ARCReader
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(ARCReader arcReader)
	throws IOException {
		arcReader.setParseHttpHeaders(true);

		Adapter<ArchiveRecord,ARCRecord> adapter1 =
			new ArchiveRecordToARCRecordAdapter();

		ARCRecordToSearchResultAdapter adapter2 =
			new ARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		
		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(arcReader,arcReader.iterator());

		CloseableIterator<ARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord,ARCRecord>(itr1,adapter1);
		
		return new AdaptedIterator<ARCRecord,CaptureSearchResult>(itr2,adapter2);
	}
	
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private class ArchiveRecordToARCRecordAdapter 
	implements Adapter<ArchiveRecord,ARCRecord> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public ARCRecord adapt(ArchiveRecord o) {
			ARCRecord rec = null;
			if(o instanceof ARCRecord) {
				rec = (ARCRecord) o;
			}
			return rec;
		}
	}
}
