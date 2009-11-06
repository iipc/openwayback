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
