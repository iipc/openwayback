/* WarcIndexer
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;

import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

public class WarcIndexer {

	private UrlCanonicalizer canonicalizer = null;
	private boolean processAll = false;
	public WarcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	public boolean isProcessAll() {
		return processAll;
	}

	public void setProcessAll(boolean processAll) {
		this.processAll = processAll;
	}

	
	/**
	 * @param warc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(File warc)
			throws IOException {
		return iterator(WARCReaderFactory.get(warc));
	}
	/**
	 * @param pathOrUrl
	 * @return Iterator of SearchResults for input pathOrUrl
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(String pathOrUrl)
			throws IOException {
		return iterator(WARCReaderFactory.get(pathOrUrl));
	}
	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(WARCReader reader)
			throws IOException {

		Adapter<ArchiveRecord, WARCRecord> adapter1 = new ArchiveRecordToWARCRecordAdapter();

		WARCRecordToSearchResultAdapter adapter2 = 
			new WARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		adapter2.setProcessAll(processAll);

		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(reader,reader.iterator());

		CloseableIterator<WARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord, WARCRecord>(itr1, adapter1);

		return new AdaptedIterator<WARCRecord, CaptureSearchResult>(itr2, adapter2);
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private class ArchiveRecordToWARCRecordAdapter implements
			Adapter<ArchiveRecord, WARCRecord> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public WARCRecord adapt(ArchiveRecord o) {
			WARCRecord rec = null;
			if (o instanceof WARCRecord) {
				rec = (WARCRecord) o;
			}
			return rec;
		}
	}
}
